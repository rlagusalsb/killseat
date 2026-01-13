import "../css/Signup.css";
import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import api from "../api/client";

export default function Signup() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    email: "",
    password: "",
    passwordConfirm: "",
    name: "",
  });
  const [error, setError] = useState("");
  const [emailMessage, setEmailMessage] = useState({ text: "", color: "" });
  const [loading, setLoading] = useState(false);
  const [isEmailChecked, setIsEmailChecked] = useState(false);

  const isPasswordMatch = form.password && form.password === form.passwordConfirm;

  const validateEmailFormat = (email) => {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  };

  const onChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));

    if (name === "email") {
      setIsEmailChecked(false);
      if (value.length > 0 && !validateEmailFormat(value)) {
        setEmailMessage({ text: "이메일 형식이 올바르지 않습니다.", color: "#ff3b3b" });
      } else {
        setEmailMessage({ text: "", color: "" });
      }
    }
  };

  const handleCheckEmail = async () => {
    if (!validateEmailFormat(form.email)) {
      setEmailMessage({ text: "유효한 이메일을 입력해주세요.", color: "#ff3b3b" });
      return;
    }
    
    try {
      await api.get("/api/members/check-email", { params: { email: form.email } });
      setIsEmailChecked(true);
      setEmailMessage({ text: "✓ 사용 가능한 이메일입니다.", color: "#28a745" });
    } catch (err) {
      if (err.response?.status === 409) {
        setEmailMessage({ text: "✗ 이미 사용 중인 이메일입니다.", color: "#ff3b3b" });
      } else {
        setEmailMessage({ text: "확인 중 오류가 발생했습니다.", color: "#ff3b3b" });
      }
      setIsEmailChecked(false);
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!isEmailChecked) {
      setError("이메일 중복 확인을 완료해주세요.");
      return;
    }
    setLoading(true);
    try {
      await api.post("/api/members/signup", {
        email: form.email,
        password: form.password,
        name: form.name
      });
      alert("회원가입 완료!");
      navigate("/login");
    } catch (err) {
      setError(err.response?.data?.message || "회원가입 실패");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="signup-page">
      <section className="signup-card">
        <h1 className="signup-title">회원가입</h1>
        <form className="signup-form" onSubmit={onSubmit}>
          
          <div className="field">
            <label>이메일</label>
            <div style={{ display: 'flex', gap: '8px' }}>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={onChange}
                style={{
                  borderColor: isEmailChecked ? '#28a745' : (emailMessage.color === '#ff3b3b' ? '#ff3b3b' : '#ccc'),
                  flex: 1
                }}
                placeholder="example@test.com"
                required
              />
              <button 
                type="button" 
                onClick={handleCheckEmail}
                disabled={isEmailChecked || !validateEmailFormat(form.email)}
                style={{
                  padding: '0 12px',
                  backgroundColor: (isEmailChecked || !validateEmailFormat(form.email)) ? '#ccc' : '#333',
                  color: '#fff',
                  border: 'none',
                  borderRadius: '4px',
                  cursor: (isEmailChecked || !validateEmailFormat(form.email)) ? 'default' : 'pointer'
                }}
              >
                {isEmailChecked ? "확인완료" : "중복확인"}
              </button>
            </div>
            {emailMessage.text && (
              <p style={{ color: emailMessage.color, fontSize: '12px', marginTop: '5px' }}>
                {emailMessage.text}
              </p>
            )}
          </div>

          <div className="field">
            <label>이름</label>
            <input type="text" name="name" value={form.name} onChange={onChange} placeholder="2자 이상" required />
          </div>

          <div className="field">
            <label>비밀번호</label>
            <input type="password" name="password" value={form.password} onChange={onChange} placeholder="8자 이상" required />
          </div>

          <div className="field">
            <label>비밀번호 확인</label>
            <input type="password" name="passwordConfirm" value={form.passwordConfirm} onChange={onChange} placeholder="비밀번호 재입력" required />
            {form.passwordConfirm && (
              <p style={{ fontSize: "12px", color: isPasswordMatch ? "#28a745" : "#ff3b3b", marginTop: "5px" }}>
                {isPasswordMatch ? "✓ 비밀번호가 일치합니다." : "✗ 비밀번호가 일치하지 않습니다."}
              </p>
            )}
          </div>

          {error && <p style={{ color: "#ff3b3b", fontSize: "14px", margin: "10px 0", textAlign: 'center' }}>{error}</p>}

          <button 
            type="submit" 
            className="signup-btn" 
            disabled={loading || !isEmailChecked || !isPasswordMatch}
          >
            {loading ? "처리 중..." : "회원가입"}
          </button>

          <p className="signup-footer">
            이미 계정이 있나요? <Link to="/login">로그인</Link>
          </p>
        </form>
      </section>
    </main>
  );
}