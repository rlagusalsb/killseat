import { Link } from "react-router-dom";
import "../css/Header.css";

export default function Header() {
  return (
    <header className="header">
      <Link to="/" className="logo">
        <img
          src="/killseatlogo.png"
          alt="Killseat Logo"
          className="logo-img"
        />
      </Link>
      <nav className="nav">
        <Link to="/">홈</Link>
        <Link to="/board">게시판</Link>
        <Link to="/my-reservations">내 예약</Link>
        <Link to="/login">로그인</Link>
      </nav>
    </header>
  );
}
