import "../css/Performance.css";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";

export default function Performance() {
  const [performances, setPerformances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    api.get("/api/performances")
      .then((res) => {
        setPerformances(Array.isArray(res.data) ? res.data : []);
      })
      .catch((err) => {
        setError("공연 목록을 불러오지 못했습니다.");
        console.error("공연 목록 로딩 에러:", err.response?.data);
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const handleReservation = (performanceId) => {
    navigate(`/waiting?performanceId=${performanceId}`);
  };

  return (
    <main className="home">
      <section className="page">
        <header className="page-header">
          <h1 className="page-title">공연 목록</h1>
        </header>

        {loading && <p className="helper-text">불러오는 중...</p>}
        {!loading && error && <p className="helper-text error">{error}</p>}

        {!loading && !error && (
          <div className="performance-list">
            {performances.map((p) => {
              const isOpen = p.status === "OPEN";
              return (
                <article key={p.performanceId} className="performance-card">
                  <img
                    className="performance-thumbnail"
                    src={p.thumbnailUrl || "/placeholder.jpg"}
                    alt={p.title}
                    loading="lazy"
                    onError={(e) => { e.currentTarget.src = "/placeholder.jpg"; }}
                  />
                  <div className="performance-info">
                    <h3 className="performance-title">{p.title}</h3>
                    <p className="performance-meta">
                      {new Date(p.startTime).toLocaleString()}
                    </p>
                    <p className={`performance-status ${isOpen ? "open" : "closed"}`}>
                      {isOpen ? "예매 가능" : "예매 마감"}
                    </p>
                  </div>
                  <button
                    className="action-button"
                    disabled={!isOpen}
                    onClick={() => handleReservation(p.performanceId)}
                  >
                    예약하기
                  </button>
                </article>
              );
            })}
            {performances.length === 0 && <p className="helper-text">등록된 공연이 없습니다.</p>}
          </div>
        )}
      </section>
    </main>
  );
}