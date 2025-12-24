import "../css/PerformanceSeats.css";
import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";

export default function PerformanceSeats() {
  const { performanceId } = useParams();
  const navigate = useNavigate();

  const [performance, setPerformance] = useState(null);
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(true);
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
      .catch((err) => {
        console.log("status:", err?.response?.status);
        console.log("data:", err?.response?.data);
        console.log("msg:", err?.message);
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

  const onPay = () => {
    if (!selectedSeatId) return;
    const ok = window.confirm("결제하시겠습니까?");
    if (!ok) return;

    alert(`선택 좌석: ${selectedSeat?.seatNumber} (다음 단계에서 결제 연동)`);
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
                disabled={!available}
                onClick={() => onClickSeat(s)}
                title={s.status}
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

          <button className="seat-pay" disabled={!selectedSeatId} onClick={onPay}>
            결제하기
          </button>
        </footer>
      </section>
    </main>
  );
}
