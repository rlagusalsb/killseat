import "../css/Home.css";
import { useNavigate } from "react-router-dom";

export default function Home() {
  const navigate = useNavigate();

  return (
    <main className="home">
      <section className="home-hero">
        <h1 className="home-title">Killseat</h1>
        <p className="home-subtitle">
          빠르고 정확한 좌석 예매 서비스
        </p>

        <button
          className="home-cta"
          onClick={() => navigate("/performances")}
        >
          공연 목록 보러가기
        </button>
      </section>
    </main>
  );
}
