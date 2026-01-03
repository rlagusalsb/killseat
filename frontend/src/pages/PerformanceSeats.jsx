import "../css/PerformanceSeats.css";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
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
    setLoading(true);
    setError("");
    setSelectedSeatId(null);

    Promise.all([
      axios.get(`/api/performances/${performanceId}`),
      axios.get(`/api/performance-seats/${performanceId}`),
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

  const selectedSeat = useMemo(() => {
    if (!selectedSeatId) return null;
    return seats.find((s) => s.performanceSeatId === selectedSeatId) || null;
  }, [selectedSeatId, seats]);

  const isAvailable = (seat) => seat.status === "AVAILABLE";

  const onClickSeat = (seat) => {
    if (!isAvailable(seat)) return;
    setSelectedSeatId((prev) =>
      prev === seat.performanceSeatId ? null : seat.performanceSeatId
    );
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

    try {
      const reservationRes = await api.post("/api/reservations", {
        performanceSeatId: selectedSeatId,
      });

      const reservationId =
        reservationRes.data?.reservationId ?? reservationRes.data?.id;

      if (!reservationId) throw new Error("reservationId 없음");

      const prepareRes = await api.post("/api/payments/prepare", {
        reservationId,
      });

      const merchantUid =
        prepareRes.data?.merchantUid ?? prepareRes.data?.merchant_uid;
      const amount = prepareRes.data?.amount;
      const name = prepareRes.data?.name ?? performance?.title ?? "Killseat 결제";

      if (!merchantUid || typeof amount !== "number") {
        throw new Error("결제 준비 데이터 오류");
      }

      const IMP = window.IMP;
      IMP.init(import.meta.env.VITE_PORTONE_IMP_CODE);

      IMP.request_pay(
        {
          pg: "html5_inicis",
          pay_method: "card",
          merchant_uid: merchantUid,
          name,
          amount,
        },
        async (rsp) => {
          try {
            if (!rsp.success) {
              alert("결제가 취소되었습니다.");
              return;
            }

            await api.post("/api/payments/confirm", {
              impUid: rsp.imp_uid,
              merchantUid: rsp.merchant_uid,
            });

            alert("결제가 완료되었습니다.");
            navigate("/my-reservations");
          } catch {
            alert("결제 검증 중 오류가 발생했습니다.");
          } finally {
            setPaying(false);
          }
        }
      );
    } catch {
      alert("결제 진행 중 오류가 발생했습니다.");
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
            </p>
          </div>

          <button className="seat-back" onClick={() => navigate(-1)}>
            공연 목록
          </button>
        </header>

        <div className="stage">STAGE</div>

        <div className="seat-grid">
          {seats.map((s) => {
            const available = isAvailable(s);
            const selected = selectedSeatId === s.performanceSeatId;

            return (
              <button
                key={s.performanceSeatId}
                className={`seat-item ${
                  available ? "available" : "reserved"
                } ${selected ? "selected" : ""}`}
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

          <button
            className="seat-pay"
            disabled={!selectedSeatId || paying}
            onClick={onPay}
          >
            {paying ? "결제 진행중..." : "결제하기"}
          </button>
        </footer>
      </section>
    </main>
  );
}
