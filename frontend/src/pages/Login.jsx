import "../css/login.css";
import { Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import api from "../api/client";
import { token } from "../auth/token";

export default function Login() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      const res = await api.post("/api/auth/login", { email, password });
      token.setAccess(res.data.accessToken);
      token.setRefresh(res.data.refreshToken);
      navigate("/");
    } catch (err) {
      alert(err.response?.data?.message || "로그인 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <h2>로그인</h2>
      <form className="login-form" onSubmit={handleSubmit}>
        <div className="input-group">
          <label htmlFor="email">아이디</label>
          <input
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            autoComplete="username"
          />
        </div>
        <div className="input-group">
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </div>
        <button type="submit" className="login-button" disabled={loading}>
          {loading ? "로그인 중..." : "로그인"}
        </button>
      </form>
      <p className="signup-text">
        아이디가 없으신가요?{" "}
        <Link to="/signup" className="signup-link">
          회원가입
        </Link>
      </p>
    </div>
  );
}
