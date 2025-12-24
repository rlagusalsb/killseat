import "../css/Signup.css";
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";

export default function Signup() {
  const navigate = useNavigate();

  const [form, setForm] = useState({
    email: "",
    password: "",
    name: "",
  });

  const [error, setError] = useState("");

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validate = () => {
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
      return "이메일 형식이 올바르지 않습니다.";
    }

    if (form.password.length < 8) {
      return "비밀번호는 8자 이상이어야 합니다.";
    }

    if (form.name.trim().length < 2) {
      return "이름은 2자 이상 입력하세요.";
    }

    return "";
  };

  const onSubmit = (e) => {
    e.preventDefault();
    setError("");

    const msg = validate();
    if (msg) {
      setError(msg);
      return;
    }

    // TODO: POST /api/members/signup
    alert("회원가입 완료 (포폴용)");
    navigate("/login");
  };

  return (
    <main className="signup-page">
      <section className="signup-card">
        <h1 className="signup-title">회원가입</h1>

        <form className="signup-form" onSubmit={onSubmit}>
          <div className="field">
            <label>이메일</label>
            <input
              type="email"
              name="email"
              value={form.email}
              onChange={onChange}
              placeholder="example@killseat.com"
            />
          </div>

          <div className="field">
            <label>비밀번호</label>
            <input
              type="password"
              name="password"
              value={form.password}
              onChange={onChange}
              placeholder="8자 이상"
            />
          </div>

          <div className="field">
            <label>이름</label>
            <input
              type="text"
              name="name"
              value={form.name}
              onChange={onChange}
              placeholder="홍길동"
            />
          </div>

          {error && <p className="form-error">{error}</p>}

          <button type="submit" className="signup-btn">
            회원가입
          </button>

          <p className="signup-footer">
            이미 계정이 있나요? <Link to="/login">로그인</Link>
          </p>
        </form>
      </section>
    </main>
  );
}
