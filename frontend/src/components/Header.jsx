import { Link } from "react-router-dom";
import "../css/Header.css";

export default function Header() {
  return (
    <header className="header">
      <h1 className="logo">Killseat</h1>
      <nav className="nav">
        <Link to="/">홈</Link>
        <Link to="/reservation">예약</Link>
        <Link to="/myseats">내 좌석</Link>
        <Link to="/login">로그인</Link>
      </nav>
    </header>
  );
}
