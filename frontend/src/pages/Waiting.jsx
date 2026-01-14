import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/client";
import { token } from "../auth/token";
import "../css/Waiting.css";

export default function Waiting() {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();

  const performanceId = searchParams.get("performanceId") || location.state?.performanceId;

  const [loading, setLoading] = useState(true);
  const [joined, setJoined] = useState(false);
  const [positionAhead, setPositionAhead] = useState(null);
  const [performance, setPerformance] = useState(null);
  const [status, setStatus] = useState("WAITING");
  const [message, setMessage] = useState("");

  const sseRef = useRef(null);
  const pollRef = useRef(null);

  const cleanup = () => {
    if (sseRef.current) { sseRef.current.close(); sseRef.current = null; }
    if (pollRef.current) { clearInterval(pollRef.current); pollRef.current = null; }
  };

  const goReservation = () => {
    cleanup();
    navigate(`/performances/${performanceId}/seats`, { replace: true });
  };

  const fetchPerformanceInfo = async () => {
    try {
      const res = await api.get(`/api/performances/${performanceId}`);
      setPerformance(res.data);
    } catch (e) {
      console.error(e);
    }
  };

  const applyStatusData = (data) => {
    const rawPosition = data?.positionAhead ?? data?.ahead ?? data?.position ?? data?.rank;
    if (rawPosition !== undefined && rawPosition !== null) {
      const currentRank = Number(rawPosition);
      const aheadCount = currentRank - 1;
      setPositionAhead(aheadCount < 0 ? 0 : aheadCount);
      if (currentRank <= 1) goReservation();
    }
    if (data?.status === "ENTER" || data?.canEnter === true) goReservation();
  };

  const fetchStatus = async () => {
    try {
      const res = await api.get("/api/queue/status", { params: { performanceId } });
      applyStatusData(res.data);
    } catch (e) { console.error(e); }
  };

  const startPolling = () => {
    if (pollRef.current) return;
    fetchStatus();
    pollRef.current = setInterval(fetchStatus, 3000);
  };

  const connectSSE = () => {
    const userInfo = JSON.parse(localStorage.getItem("user") || "{}");
    const mId = userInfo.memberId || userInfo.id || "1";
    const baseURL = import.meta.env.VITE_API_URL || "http://localhost:8080";
    const url = `${baseURL}/api/queue/subscribe/${mId}`;

    try {
      const es = new EventSource(url, { withCredentials: true });
      sseRef.current = es;
      es.addEventListener("update", (event) => {
        try {
          const data = JSON.parse(event.data);
          applyStatusData(data);
        } catch {
          const n = Number(event.data);
          if (!Number.isNaN(n)) applyStatusData({ rank: n });
        }
      });
      es.addEventListener("proceed", () => goReservation());
      es.onerror = () => { cleanup(); startPolling(); };
    } catch { startPolling(); }
  };

  const joinQueue = async () => {
    if (!performanceId) {
      setStatus("ERROR");
      setMessage("공연 정보를 찾을 수 없습니다.");
      setLoading(false);
      return;
    }
    try {
      await api.post("/api/queue/join", { performanceId: Number(performanceId) });
      setJoined(true);
      fetchPerformanceInfo();
    } catch (e) {
      setStatus("ERROR");
      setMessage(e.response?.data?.message || "대기열 진입 실패");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token.getAccess()) {
      alert("로그인이 필요합니다.");
      navigate("/login", { state: { from: location } });
      return;
    }
    joinQueue();
    return () => cleanup();
  }, [performanceId]);

  useEffect(() => {
    if (joined) {
      fetchStatus();
      connectSSE();
    }
  }, [joined]);

  if (loading) {
    return (
      <div className="waiting-wrap">
        <div className="waiting-card">
          <div className="spinner"></div>
          <h2 className="waiting-title">대기열 확인 중...</h2>
        </div>
      </div>
    );
  }

  if (status === "ERROR") {
    return (
      <div className="waiting-wrap">
        <div className="waiting-card">
          <h2 className="waiting-title">안내</h2>
          <p className="waiting-sub">{message}</p>
          <button className="btn-cancel" onClick={() => navigate("/performances")}>목록으로</button>
        </div>
      </div>
    );
  }

  return (
    <div className="waiting-wrap">
      {performance && (
        <div className="performance-mini-card">
          <img src={performance.thumbnailUrl || "/placeholder.jpg"} alt="thumb" className="mini-thumbnail" />
          <div className="mini-info">
            <h4>{performance.title}</h4>
            <p>{new Date(performance.startTime).toLocaleString()}</p>
          </div>
        </div>
      )}

      <div className="waiting-card">
        <h2 className="waiting-title">예약 대기열</h2>
        
        <div className="waiting-box">
          <div className="waiting-label">내 앞 대기 인원</div>
          <div className="waiting-big">
            {positionAhead !== null ? positionAhead : "0"}
            <span>명</span>
          </div>
        </div>

        <p className="waiting-sub">
          현재 접속 인원이 많아 대기 중입니다.<br />
          잠시만 기다려 주시면 예약 페이지로 연결됩니다.
        </p>

        <div className="waiting-actions">
          <button className="btn-cancel" onClick={() => navigate("/performances")}>
            대기 취소
          </button>
        </div>
      </div>
    </div>
  );
}