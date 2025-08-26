import "../css/login.css";
import { Link } from "react-router-dom";

export default function Login() {
  return (
    <div className="login-container">
      <h2>로그인</h2>
      <form className="login-form">
        <div className="input-group">
          <label htmlFor="username">아이디</label>
          <input type="text" id="username" />
        </div>

        <div className="input-group">
          <label htmlFor="password">비밀번호</label>
          <input type="password" id="password" />
        </div>

        <button type="submit" className="login-button">
          로그인
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
