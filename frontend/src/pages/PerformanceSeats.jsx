import "../css/PerformanceSeats.css";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import api from "../api/client";

export default function PerformanceSeats() {
  const { performanceId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();

  const queryParams = new URLSearchParams(location.search);
  const scheduleId = queryParams.get("scheduleId");

  const [performance, setPerformance] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState("");
  const [selectedSeatId, setSelectedSeatId] = useState(null);

  useEffect(() => {
    const impCode = import.meta.env.VITE_IMP_CODE;
    if (window.IMP && impCode) {
      window.IMP.init(impCode);
    }
  }, []);

  useEffect(() => {
    if (!scheduleId) {
      setError("회차 정보가 없습니다.");
      setLoading(false);
      return;
    }

    setLoading(true);
    setError("");
    setSelectedSeatId(null);

    Promise.all([
      api.get(`/api/performances/${performanceId}`),
      api.get(`/api/performance-seats/${performanceId}?scheduleId=${scheduleId}`),
    ])
      .then(([pRes, sRes]) => {
        setPerformance(pRes.data);
        setSeats(Array.isArray(sRes.data) ? sRes.data : []);
      })
      .catch(() => {
        setError("좌석 정보를 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, [performanceId, scheduleId]);

  const sortedSeats = useMemo(() => {
    const parse = (seatNumber) => {
      const m = String(seatNumber ?? "").trim().match(/^([A-Za-z])(\d+)$/);
      if (!m) return null;
      return { row: m[1].toUpperCase(), col: Number(m[2]) };
    };

    return [...seats].sort((a, b) => {
      const pa = parse(a.seatNumber);
      const pb = parse(b.seatNumber);
      if (!pa && !pb) return 0;
      if (!pa) return 1;
      if (!pb) return -1;
      const rowCmp = pa.row.localeCompare(pb.row);
      if (rowCmp !== 0) return rowCmp;
      return pa.col - pb.col;
    });
  }, [seats]);

  const groupedSeats = useMemo(() => {
    const groups = {};
    sortedSeats.forEach((seat) => {
      const rowLabel = String(seat.seatNumber).match(/^[A-Za-z]+/)?.[0] || "?";
      if (!groups[rowLabel]) groups[rowLabel] = [];
      groups[rowLabel].push(seat);
    });
    return groups;
  }, [sortedSeats]);

  const selectedSeat = useMemo(() => {
    if (!selectedSeatId) return null;
    return sortedSeats.find((s) => s.performanceSeatId === selectedSeatId) || null;
  }, [selectedSeatId, sortedSeats]);

  const isAvailable = (seat) => seat.status === "AVAILABLE";

  const onClickSeat = (seat) => {
    if (!isAvailable(seat)) return;
    setSelectedSeatId((prev) =>
      prev === seat.performanceSeatId ? null : seat.performanceSeatId
    );
  };

  const formatPrice = (price) => {
    if (price == null) return "-";
    return `${Number(price).toLocaleString()}원`;
  };

  const onPay = async () => {
    if (!selectedSeatId || !selectedSeat) return;
    if (!window.confirm("결제하시겠습니까?")) return;
    if (!window.IMP) return alert("결제 라이브러리 로드 실패");

    setPaying(true);
    let reservationId = null;

    try {
      const reservationRes = await api.post(`/api/reservations/${selectedSeatId}`);
      reservationId = reservationRes.data?.reservationId ?? reservationRes.data?.id;
      
      const prepareRes = await api.post("/api/payments/prepare", {
        reservationId,
        method: "CARD",
      });

      const { merchantUid, amount, name } = prepareRes.data;
      const userInfo = JSON.parse(localStorage.getItem("user") || "{}");

      window.IMP.request_pay(
        {
          pg: "kakaopay.TC0ONETIME",
          pay_method: "card",
          merchant_uid: merchantUid,
          name: name || performance?.title,
          amount,
          buyer_email: userInfo.email || "",
          buyer_name: userInfo.name || "구매자",
        },
        async (rsp) => {
          if (rsp?.success) {
            await api.post("/api/payments/confirm", {
              impUid: rsp.imp_uid,
              merchantUid,
            });
            alert("결제 완료");
            navigate("/my-reservations");
          } else {
            if (reservationId) await api.delete(`/api/reservations/${reservationId}`);
            alert(rsp?.error_msg || "결제 취소");
          }
          setPaying(false);
        }
      );
    } catch (err) {
      console.error(err);
      if (reservationId) await api.delete(`/api/reservations/${reservationId}`).catch(() => {});
      alert("오류 발생");
      setPaying(false);
    }
  };

  if (loading) return <main className="seat-page"><p className="seat-helper">로딩 중...</p></main>;
  if (error) return <main className="seat-page"><p className="seat-helper">{error}</p></main>;

  return (
    <main className="seat-page">
      <section className="seat-container">
        <header className="seat-header">
          <img className="seat-thumbnail" src={performance?.thumbnailUrl || "/placeholder.jpg"} alt="poster" />
          <div className="seat-header-info">
            <h1 className="seat-title">{performance?.title}</h1>
            <p className="seat-meta">
               회차번호: {scheduleId}
               {" · "}
              <span className="seat-price">{formatPrice(performance?.price)}</span>
            </p>
          </div>
        </header>

        <div className="stage">STAGE</div>

        <div className="seat-area">
          {Object.entries(groupedSeats).map(([rowLabel, rowSeats]) => (
            <div key={rowLabel} className="seat-row">
              <div className="seat-row-label">{rowLabel}</div>
              <div className="seat-row-items">
                {rowSeats.map((s) => (
                  <button
                    key={s.performanceSeatId}
                    className={`seat-item ${isAvailable(s) ? "available" : "reserved"} ${selectedSeatId === s.performanceSeatId ? "selected" : ""}`}
                    disabled={!isAvailable(s) || paying}
                    onClick={() => onClickSeat(s)}
                  >
                    {s.seatNumber.replace(/^[A-Za-z]+/, "")}
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>

        <footer className="seat-footer">
          <div className="seat-summary">
            <span className="seat-summary-label">선택 좌석</span>
            <span className="seat-summary-value">{selectedSeat ? selectedSeat.seatNumber : "-"}</span>
          </div>
          <div className="seat-actions">
            <button className="seat-back" onClick={() => navigate(-1)} disabled={paying}>뒤로가기</button>
            <button className="seat-pay" disabled={!selectedSeatId || paying} onClick={onPay}>
              {paying ? "진행 중..." : "결제하기"}
            </button>
          </div>
        </footer>
      </section>
    </main>
  );
}