import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import api from "../api/client";
import "../css/PerformanceDetail.css";

export default function PerformanceDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [performance, setPerformance] = useState(null);

  useEffect(() => {
    api.get(`/api/performances/${id}`)
      .then(res => setPerformance(res.data))
      .catch(err => console.error(err));
  }, [id]);

  if (!performance) return <div className="helper-text">로딩 중...</div>;

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false });
  };

  return (
    <div className="detail-container">
      <div className="detail-header">
        <img src={performance.thumbnailUrl} alt={performance.title} className="detail-poster" />
        <div className="detail-main-info">
          <span className={`status-badge ${performance.status === "OPEN" ? "open" : "closed"}`}>
            {performance.status === "OPEN" ? "예매 가능" : "예매 종료"}
          </span>
          <h1>{performance.title}</h1>
          <p className="detail-location">장소: {performance.location}</p>
          <p className="detail-price">가격: {performance.price?.toLocaleString()}원</p>
        </div>
      </div>

      <div className="detail-content">
        <h3>공연 설명</h3>
        <p>{performance.content}</p>
      </div>

      <div className="detail-schedules">
        <h3>회차 선택</h3>
        <div className="schedule-grid">
          {performance.schedules?.map(sc => (
            <button
              key={sc.scheduleId}
              className="sch-btn-large"
              disabled={performance.status !== "OPEN"}
              onClick={() => navigate(`/waiting?performanceId=${id}&scheduleId=${sc.scheduleId}`)}
            >
              <span className="sch-date">{formatDate(sc.startTime)}</span>
              <span className="sch-action">예매하기</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}