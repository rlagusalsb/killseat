import { useEffect, useState } from "react";
import api from "../api/client";
import "../css/MyReservations.css";

const STATUS_LABEL = {
  PENDING: "결제 대기",
  CONFIRMED: "예약 완료",
  CANCELED: "취소됨",
};

const statusLabel = (s) => STATUS_LABEL[s] ?? s ?? "-";

const statusClass = (s) => {
  const key = String(s || "").toLowerCase();
  return `myresv-status myresv-status--${key}`;
};

export default function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      const res = await api.get("/api/mypage/reservations");
      setReservations(Array.isArray(res.data) ? res.data : []);
    } catch (e) {
      console.error(e);
      alert("예약 정보를 불러오지 못했습니다.");
      setReservations([]);
    } finally {
      setLoading(false);
    }
  };

  const cancelPayment = async (paymentId) => {
    if (!paymentId) return;
    if (!window.confirm("결제를 취소하시겠습니까?")) return;

    try {
      await api.post(`/api/mypage/payments/${paymentId}/cancel`, {
        reason: "변심",
      });
      alert("결제가 취소되었습니다.");
      fetchReservations();
    } catch (e) {
      console.error(e);
      alert("결제 취소에 실패했습니다.");
    }
  };

  if (loading) return <p className="myresv-loading">로딩 중...</p>;

  return (
    <div className="myresv-page">
      <div className="myresv-header">
        <h2 className="myresv-title">내 예약</h2>
        <p className="myresv-sub">내가 예약한 좌석들을 확인할 수 있습니다.</p>
      </div>

      {reservations.length === 0 ? (
        <div className="myresv-empty">
          <p className="myresv-empty-title">예약 내역이 없습니다.</p>
          <p className="myresv-empty-sub">공연을 선택하고 좌석을 예매해보세요.</p>
        </div>
      ) : (
        <div className="myresv-list">
          {reservations.map((r) => (
            <div key={r.reservationId} className="myresv-card">
              <img
                className="myresv-thumb"
                src={r.performanceThumbnailUrl}
                alt=""
              />

              <div className="myresv-body">
                <div className="myresv-row">
                  <h4 className="myresv-perf">{r.performanceTitle}</h4>

                  <span className={statusClass(r.reservationStatus)}>
                    {statusLabel(r.reservationStatus)}
                  </span>
                </div>

                <div className="myresv-meta">
                  <div className="myresv-meta-item">
                    <span className="myresv-meta-label">좌석</span>
                    <span className="myresv-meta-value">{r.seatInfo}</span>
                  </div>

                  {r.reservedAt && (
                    <div className="myresv-meta-item">
                      <span className="myresv-meta-label">예약일</span>
                      <span className="myresv-meta-value">
                        {new Date(r.reservedAt).toLocaleString()}
                      </span>
                    </div>
                  )}
                </div>

                {r.reservationStatus === "CONFIRMED" && r.paymentId && (
                  <div className="myresv-actions">
                    <button
                      className="myresv-btn myresv-btn--danger"
                      onClick={() => cancelPayment(r.paymentId)}
                    >
                      결제 취소
                    </button>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
