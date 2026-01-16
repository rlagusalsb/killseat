import "../css/PerformanceSeats.css";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import api from "../api/client";

export default function PerformanceSeats() {
  const { performanceId } = useParams();
  const navigate = useNavigate();

  const [performance, setPerformance] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState("");
  const [selectedSeatId, setSelectedSeatId] = useState(null);

  useEffect(() => {
    if (window.IMP) {
      window.IMP.init("imp81861316");
    }
  }, []);

  useEffect(() => {
    setLoading(true);
    setError("");
    setSelectedSeatId(null);

    Promise.all([
      api.get(`/api/performances/${performanceId}`),
      api.get(`/api/performance-seats/${performanceId}`),
    ])
      .then(([pRes, sRes]) => {
        setPerformance(pRes.data);
        setSeats(Array.isArray(sRes.data) ? sRes.data : []);
      })
      .catch(() => {
        setError("좌석 정보를 불러오지 못했습니다.");
      })
      .finally(() => setLoading(false));
  }, [performanceId]);

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

  const selectedSeat = useMemo(() => {
    if (!selectedSeatId) return null;
    return (
      sortedSeats.find((s) => s.performanceSeatId === selectedSeatId) || null
    );
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
    const n = Number(price);
    if (Number.isNaN(n)) return "-";
    return `${n.toLocaleString()}원`;
  };

  const onPay = async () => {
    if (!selectedSeatId || !selectedSeat) return;

    const ok = window.confirm("결제하시겠습니까?");
    if (!ok) return;

    if (!window.IMP) {
      alert("PortOne SDK가 로드되지 않았습니다.");
      return;
    }

    setPaying(true);

    let reservationId = null;
    let preparedMerchantUid = null;

    try {
      const reservationRes = await api.post(
        `/api/reservations/${selectedSeatId}`
      );

      reservationId =
        reservationRes.data?.reservationId ?? reservationRes.data?.id;
      if (!reservationId) throw new Error("reservationId 없음");

      const prepareRes = await api.post("/api/payments/prepare", {
        reservationId,
        method: "CARD",
      });

      preparedMerchantUid =
        prepareRes.data?.merchantUid ?? prepareRes.data?.merchant_uid;

      const amount = prepareRes.data?.amount;
      const name = prepareRes.data?.name ?? performance?.title ?? "Killseat 결제";

      if (!preparedMerchantUid || typeof amount !== "number") {
        throw new Error("결제 준비 데이터 오류");
      }

      const IMP = window.IMP;

      IMP.request_pay(
        {
          pg: "kakaopay.TC0ONETIME",
          pay_method: "card",
          merchant_uid: preparedMerchantUid,
          name,
          amount,
          buyer_email: "test@test.com",
          buyer_name: "테스터",
          buyer_tel: "01012341234",
        },
        async (rsp) => {
          try {
            if (!rsp?.success) {
              if (reservationId) {
                await api.delete(`/api/reservations/${reservationId}`);
              }
              alert("결제가 취소되었습니다.");
              setPaying(false);
              return;
            }

            await api.post("/api/payments/confirm", {
              impUid: rsp.imp_uid,
              merchantUid: preparedMerchantUid,
            });

            alert("결제가 완료되었습니다.");
            navigate("/my-reservations");
          } catch {
            alert("결제 오류가 발생했습니다.");
          } finally {
            setPaying(false);
          }
        }
      );
    } catch (err) {
      if (reservationId) {
        await api.delete(`/api/reservations/${reservationId}`).catch(() => {});
      }
      console.error("결제 준비 오류:", err.response?.data);
      setPaying(false);
    }
  };

  if (loading) {
    return (
      <main className="seat-page">
        <p className="seat-helper">불러오는 중...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="seat-page">
        <p className="seat-helper seat-error">{error}</p>
        <button className="seat-back" onClick={() => navigate(-1)}>
          뒤로가기
        </button>
      </main>
    );
  }

  return (
    <main className="seat-page">
      <section className="seat-container">
        <header className="seat-header">
          <img
            className="seat-thumbnail"
            src={performance?.thumbnailUrl || "/placeholder.jpg"}
            alt={performance?.title || "performance"}
            onError={(e) => {
              e.currentTarget.src = "/placeholder.jpg";
            }}
          />

          <div className="seat-header-info">
            <h1 className="seat-title">{performance?.title}</h1>
            <p className="seat-meta">
              {performance?.startTime
                ? new Date(performance.startTime).toLocaleString()
                : "-"}
              {" · "}
              <span className="seat-price">{formatPrice(performance?.price)}</span>
            </p>
          </div>
        </header>

        <div className="stage">STAGE</div>

        <div className="seat-grid">
          {sortedSeats.map((s) => {
            const available = isAvailable(s);
            const selected = selectedSeatId === s.performanceSeatId;

            return (
              <button
                key={s.performanceSeatId}
                className={`seat-item ${available ? "available" : "reserved"} ${
                  selected ? "selected" : ""
                }`}
                disabled={!available || paying}
                onClick={() => onClickSeat(s)}
              >
                {s.seatNumber}
              </button>
            );
          })}
        </div>

        <footer className="seat-footer">
          <div className="seat-summary">
            <span className="seat-summary-label">선택 좌석</span>
            <span className="seat-summary-value">
              {selectedSeat ? selectedSeat.seatNumber : "-"}
            </span>
          </div>

          <div className="seat-actions">
            <button
              className="seat-back"
              onClick={() => navigate(-1)}
              disabled={paying}
            >
              공연 목록
            </button>

            <button
              className="seat-pay"
              disabled={!selectedSeatId || paying}
              onClick={onPay}
            >
              {paying ? "결제 진행중..." : "결제하기"}
            </button>
          </div>
        </footer>
      </section>
    </main>
  );
}
