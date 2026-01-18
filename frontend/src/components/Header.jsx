import { Link, useNavigate } from "react-router-dom";
import "../css/Header.css";
import { token } from "../auth/token";

export default function Header() {
  const navigate = useNavigate();
  const isLogin = !!token.getAccess();

  const userRole = localStorage.getItem("role"); 
  const isAdmin = userRole === "ROLE_ADMIN";

  const handleLogout = () => {
    token.clear();
    localStorage.removeItem("role");
    localStorage.removeItem("name");
    localStorage.removeItem("loginId");
    localStorage.removeItem("memberId");
    navigate("/login");
  };

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

        <Link to="/board">커뮤니티</Link>

        {isLogin && !isAdmin && <Link to="/my-reservations">내 예약</Link>}

        {isLogin && isAdmin && (
          <Link to="/admin" className="admin-nav-link">
            관리자
          </Link>
        )}

        {isLogin ? (
          <button className="logout-btn" onClick={handleLogout}>
            로그아웃
          </button>
        ) : (
          <Link to="/login">로그인</Link>
        )}
      </nav>
    </header>
  );
}
