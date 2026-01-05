import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../css/PostEdit.css";
import api from "../api/client";

export default function PostEdit() {
  const { postId } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");

  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(false);
  const [error, setError] = useState("");

  const canSubmit =
    (title.trim().length > 0) &&
    (content.trim().length > 0) &&
    (!loading) &&
    (!fetching);

  const formatDate = (iso) => {
    if (!iso) return "";
    return iso.replace("T", " ").slice(0, 16);
  };

  const fetchPost = async () => {
    setFetching(true);
    setError("");

    try {
      const res = await api.get(`/api/posts/${postId}`);
      const p = res.data;

      setPost(p);
      setTitle(p?.title ?? "");
      setContent(p?.content ?? "");
    } catch (e) {
      setError(
        e?.response?.data?.message ||
          e?.message ||
          "게시글을 불러오지 못했습니다."
      );
    } finally {
      setFetching(false);
    }
  };

  useEffect(() => {
    if ((postId != null) && (String(postId).trim() !== "")) {
      fetchPost();
    } else {
      setError("게시글 ID가 필요합니다.");
    }
  }, [postId]);

  const onSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if ((title == null) || (title.trim() === "")) {
      setError("제목은 필수입니다.");
      return;
    }

    if ((content == null) || (content.trim() === "")) {
      setError("내용은 필수입니다.");
      return;
    }

    if ((title.trim().length > 100)) {
      setError("제목은 100자 이하여야 합니다.");
      return;
    }

    setLoading(true);
    try {
      await api.put(`/api/posts/${postId}`, {
        title: title.trim(),
        content: content.trim(),
      });

      navigate(`/posts/${postId}`);
    } catch (e2) {
      setError(
        e2?.response?.data?.message ||
          e2?.message ||
          "게시글 수정에 실패했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="postedit-page">
      <section className="postedit-shell">
        <header className="postedit-header">
          <button
            className="postedit-back"
            type="button"
            onClick={() => navigate(`/posts/${postId}`)}
            disabled={loading || fetching}
          >
            ← 상세로
          </button>
        </header>

        <div className="postedit-card">
          <div className="postedit-titlebar">
            <h1 className="postedit-title">글 수정</h1>
          </div>

          {post && (
            <div className="postedit-meta">
              <span>작성일 {formatDate(post.createdAt)}</span>
              {post.updatedAt && post.updatedAt !== post.createdAt && (
                <span> · 수정일 {formatDate(post.updatedAt)}</span>
              )}
            </div>
          )}

          {error && <div className="postedit-alert">{error}</div>}

          {fetching && <div className="postedit-loading">불러오는 중...</div>}

          {!fetching && (
            <form className="postedit-form" onSubmit={onSubmit}>
              <div className="postedit-field">
                <label className="postedit-label" htmlFor="title">
                  제목
                </label>
                <input
                  id="title"
                  className="postedit-input"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  maxLength={100}
                  placeholder="제목 (최대 100자)"
                  disabled={loading}
                />
                <div className="postedit-hint">
                  <span>{title.trim().length}</span> / 100
                </div>
              </div>

              <div className="postedit-field">
                <label className="postedit-label" htmlFor="content">
                  내용
                </label>
                <textarea
                  id="content"
                  className="postedit-textarea"
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  placeholder="내용"
                  rows={10}
                  disabled={loading}
                />
                <div className="postedit-hint">
                  공백 제외 <span>{content.trim().length}</span>자
                </div>
              </div>

              <div className="postedit-actions">
                <button
                  className="postedit-btn is-ghost"
                  type="button"
                  onClick={() => navigate(`/posts/${postId}`)}
                  disabled={loading}
                >
                  취소
                </button>

                <button className="postedit-btn" type="submit" disabled={!canSubmit}>
                  {loading ? "저장 중..." : "저장"}
                </button>
              </div>
            </form>
          )}
        </div>
      </section>
    </main>
  );
}
