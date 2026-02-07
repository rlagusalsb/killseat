import { Link, useNavigate, useLocation } from "react-router-dom";
import { useEffect, useState } from "react";
import "../css/Header.css";
import { token } from "../auth/token";

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const [isLogin, setIsLogin] = useState(!!token.getAccess());
  const [isAdmin, setIsAdmin] = useState(localStorage.getItem("role") === "ROLE_ADMIN");

  useEffect(() => {
    const accessToken = token.getAccess();
    
    if (!accessToken) {
      setIsLogin(false);
      setIsAdmin(false);
    } else {
      setIsLogin(true);
      setIsAdmin(localStorage.getItem("role") === "ROLE_ADMIN");
    }
  }, [location]);

  const handleLogout = () => {
    token.clear();
    localStorage.removeItem("role");
    localStorage.removeItem("name");
    localStorage.removeItem("loginId");
    localStorage.removeItem("memberId");
    setIsLogin(false);
    setIsAdmin(false);
    navigate("/login");
  };

  return (
    <header className="header">
      <Link to="/" className="logo">
        <img src="/killseatlogo.png" alt="Killseat Logo" className="logo-img" />
      </Link>
      <nav className="nav">
        <Link to="/">홈</Link>
        <Link to="/board">커뮤니티</Link>

        {isLogin && !isAdmin && <Link to="/my-reservations">내 예약</Link>}

        {isLogin && isAdmin && (
          <Link to="/admin" className="admin-nav-link">관리자</Link>
        )}

        {isLogin ? (
          <button className="logout-btn" onClick={handleLogout}>로그아웃</button>
        ) : (
          <Link to="/login">로그인</Link>
        )}
      </nav>
    </header>
  );
}