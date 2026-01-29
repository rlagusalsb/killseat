import "../css/Performance.css";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../api/client";

export default function Performance() {
  const [performances, setPerformances] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [pageInfo, setPageInfo] = useState({
    pageNumber: 0,
    totalPages: 0,
    totalElements: 0
  });

  const navigate = useNavigate();

  useEffect(() => {
    fetchPerformances(pageInfo.pageNumber);
  }, [pageInfo.pageNumber]);

  const fetchPerformances = (page) => {
    setLoading(true);
    api.get("/api/performances", { params: { page, size: 20 } })
      .then((res) => {
        const { content, pageNumber, totalPages, totalElements } = res.data;
        setPerformances(content || []);
        setPageInfo({ pageNumber, totalPages, totalElements });
      })
      .catch((err) => {
        setError("공연 목록을 불러오지 못했습니다.");
        console.error(err);
      })
      .finally(() => setLoading(false));
  };

  const handlePageChange = (newPage) => {
    setPageInfo(prev => ({ ...prev, pageNumber: newPage }));
  };

  return (
    <main className="home">
      <section className="page">
        <header className="page-header">
          <h1 className="page-title">공연 목록</h1>
          {!loading && <span className="total-count">총 {pageInfo.totalElements}건</span>}
        </header>

        {loading && <p className="helper-text">로딩 중...</p>}
        {error && <p className="helper-text error">{error}</p>}

        <div className="performance-list">
          {performances.map((p) => (
            <article 
              key={p.performanceId} 
              className="performance-card clickable"
              onClick={() => navigate(`/performance/${p.performanceId}`)}
            >
              <div className="card-main">
                <img
                  className="performance-thumbnail"
                  src={p.thumbnailUrl || "https://via.placeholder.com/140x90"}
                  alt={p.title}
                />
                <div className="performance-info">
                  <div className="info-top">
                    <h3 className="performance-title">{p.title}</h3>
                    <span className={`status-badge ${p.status === "OPEN" ? "open" : "closed"}`}>
                      {p.status === "OPEN" ? "예매 가능" : "예매 종료"}
                    </span>
                  </div>
                  <p className="performance-location">{p.location}</p>
                </div>
              </div>
            </article>
          ))}
        </div>

        {!loading && pageInfo.totalPages > 0 && (
          <div className="pagination">
            <button 
              className="page-btn"
              onClick={() => handlePageChange(pageInfo.pageNumber - 1)}
              disabled={pageInfo.pageNumber === 0}
            >
              이전
            </button>
            
            <span className="page-info">
              {pageInfo.pageNumber + 1} / {pageInfo.totalPages}
            </span>

            <button 
              className="page-btn"
              onClick={() => handlePageChange(pageInfo.pageNumber + 1)}
              disabled={pageInfo.pageNumber >= pageInfo.totalPages - 1}
            >
              다음
            </button>
          </div>
        )}
      </section>
    </main>
  );
}