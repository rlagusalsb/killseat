import "../css/Home.css";
import { Link } from "react-router-dom";

export default function Home() {
  return (
    <main className="home">
      <section className="hero">
        <h1 className="hero-title">Killseat</h1>
        <p className="hero-subtitle">
          빠르고 정확한 좌석 예약 시스템, 지금 바로 시작해보세요.
        </p>
        <Link to="/reservation" className="hero-button">
          예약하기
        </Link>
      </section>
    </main>
  );
}
