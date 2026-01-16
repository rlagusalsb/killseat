import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "../css/PostWrite.css";
import api from "../api/client";

export default function PostWrite() {
  const navigate = useNavigate();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const canSubmit = 
    title.trim().length > 0 && 
    content.trim().length > 0 && 
    !loading;

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!title.trim()) {
      setError("제목은 필수입니다.");
      return;
    }
    if (!content.trim()) {
      setError("내용은 필수입니다.");
      return;
    }
    if (title.trim().length > 100) {
      setError("제목은 100자 이하여야 합니다.");
      return;
    }

    setLoading(true);
    try {
      const res = await api.post("/api/posts", {
        title: title.trim(),
        content: content.trim(),
      });

      const postId = res?.data?.postId;
      if (postId) {
        navigate(`/posts/${postId}`);
      } else {
        navigate("/board");
      }
    } catch (err) {
      console.error("게시글 작성 에러:", err.response?.data);
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="postwrite-page">
      <section className="postwrite-shell">
        <header className="postwrite-header">
          <h1 className="postwrite-title">글 작성</h1>
          <p className="postwrite-desc">커뮤니티에 글을 작성하세요.</p>
        </header>

        <form className="postwrite-card" onSubmit={onSubmit}>
          {error && <div className="postwrite-alert">{error}</div>}

          <div className="postwrite-field">
            <label className="postwrite-label" htmlFor="title">
              제목
            </label>
            <input
              id="title"
              className="postwrite-input"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              maxLength={100}
              placeholder="제목을 입력하세요 (최대 100자)"
              disabled={loading}
            />
            <div className="postwrite-hint">
              <span>{title.trim().length}</span> / 100
            </div>
          </div>

          <div className="postwrite-field">
            <label className="postwrite-label" htmlFor="content">
              내용
            </label>
            <textarea
              id="content"
              className="postwrite-textarea"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="내용을 입력하세요"
              rows={10}
              disabled={loading}
            />
            <div className="postwrite-hint">
              공백 제외 <span>{content.trim().length}</span>자
            </div>
          </div>

          <div className="postwrite-actions">
            <button
              className="postwrite-btn is-ghost"
              type="button"
              onClick={() => navigate(-1)}
              disabled={loading}
            >
              취소
            </button>

            <button 
              className="postwrite-btn" 
              type="submit" 
              disabled={!canSubmit}
            >
              {loading ? "등록 중..." : "등록"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}