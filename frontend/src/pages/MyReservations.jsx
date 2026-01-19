import { useEffect, useState } from "react";
import api from "../api/client";
import "../css/MyReservations.css";

const STATUS_LABEL = {
  PENDING: "결제 대기",
  PAYING: "결제 진행 중",
  CONFIRMED: "예약 완료",
  CANCELED: "취소됨",
};

const statusLabel = (s) => STATUS_LABEL[s] ?? s ?? "-";
const statusClass = (s) => `myresv-status myresv-status--${String(s || "").toLowerCase()}`;

export default function MyReservations() {
  const [reservations, setReservations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchReservations();
  }, []);

  const fetchReservations = async () => {
    try {
      setLoading(true);
      const res = await api.get("/api/mypage/reservations");
      setReservations(res.data.content || res.data || []);
    } catch (e) {
      console.error("예약 목록 조회 에러:", e.response?.data);
      setReservations([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = async (reservationId) => {
    if (!window.confirm("예약을 취소하시겠습니까?")) return;
    try {
      await api.post(`/api/reservations/${reservationId}/cancel`);
      alert("정상적으로 취소되었습니다.");
      fetchReservations();
    } catch (e) {
      console.error("예약 취소 에러:", e.response?.data);
    }
  };

  const formatDateTime = (dateTime) => {
    if (!dateTime) return "-";
    return new Date(dateTime).toLocaleString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
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
        </div>
      ) : (
        <div className="myresv-list">
          {reservations.map((r) => (
            <div key={r.reservationId} className="myresv-card">
              <img className="myresv-thumb" src={r.performanceThumbnailUrl || "/placeholder.jpg"} alt="" />
              <div className="myresv-body">
                <div className="myresv-row">
                  <h4 className="myresv-perf">{r.performanceTitle}</h4>
                  <span className={statusClass(r.reservationStatus)}>
                    {statusLabel(r.reservationStatus)}
                  </span>
                </div>
                <div className="myresv-meta">
                  <div className="myresv-meta-item">
                    <span className="myresv-meta-label">공연 일시</span>
                    <span className="myresv-meta-value">{formatDateTime(r.performanceStartTime)}</span>
                  </div>
                  <div className="myresv-meta-item">
                    <span className="myresv-meta-label">장소</span>
                    <span className="myresv-meta-value">{r.performanceLocation || "-"}</span>
                  </div>
                  <div className="myresv-meta-item">
                    <span className="myresv-meta-label">좌석</span>
                    <span className="myresv-meta-value">{r.seatInfo}</span>
                  </div>
                </div>
                <div className="myresv-actions">
                  {r.reservationStatus === "CONFIRMED" && (
                    <button 
                      className="myresv-btn myresv-btn--danger" 
                      onClick={() => handleCancel(r.reservationId)}
                    >
                      예약 취소
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}