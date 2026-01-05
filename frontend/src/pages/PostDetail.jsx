import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../css/PostDetail.css";
import api from "../api/client";

export default function PostDetail() {
  const { postId } = useParams();
  const navigate = useNavigate();

  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchPost = async () => {
    setLoading(true);
    setError("");

    try {
      const res = await api.get(`/api/posts/${postId}`);
      setPost(res.data);
    } catch (e) {
      setError(
        e?.response?.data?.message ||
          e?.message ||
          "게시글을 불러오지 못했습니다."
      );
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if ((postId != null) && (String(postId).trim() !== "")) {
      fetchPost();
    } else {
      setError("게시글 ID가 필요합니다.");
    }
  }, [postId]);

  const formatDate = (iso) => {
    if (!iso) return "";
    return iso.replace("T", " ").slice(0, 16);
  };

  const onDelete = async () => {
    if (!window.confirm("게시글을 삭제할까요?")) return;

    try {
      await api.delete(`/api/posts/${postId}`);
      navigate("/board");
    } catch (e) {
      alert(
        e?.response?.data?.message ||
          e?.message ||
          "게시글 삭제에 실패했습니다."
      );
    }
  };

  return (
    <main className="postdetail-page">
      <section className="postdetail-shell">
        <header className="postdetail-header">
          <button
            className="postdetail-back"
            type="button"
            onClick={() => navigate("/board")}
          >
            ← 목록
          </button>
        </header>

        <div className="postdetail-card">
          {error && <div className="postdetail-alert">{error}</div>}

          {loading && <div className="postdetail-loading">불러오는 중...</div>}

          {!loading && !error && post && (
            <>
              <h1 className="postdetail-title">{post.title}</h1>

              <div className="postdetail-meta">
                <span className="postdetail-author">{post.memberName}</span>
                <span className="postdetail-dot" />
                <span className="postdetail-date">
                  {formatDate(post.createdAt)}
                </span>
                {post.updatedAt && post.updatedAt !== post.createdAt && (
                  <>
                    <span className="postdetail-dot" />
                    <span className="postdetail-edited">수정됨</span>
                  </>
                )}
              </div>

              <div className="postdetail-content">{post.content}</div>

              <div className="postdetail-actions">
                <button
                  className="postdetail-btn is-ghost"
                  type="button"
                  onClick={() => navigate(`/posts/${postId}/edit`)}
                >
                  수정
                </button>

                <button
                  className="postdetail-btn is-danger"
                  type="button"
                  onClick={onDelete}
                >
                  삭제
                </button>
              </div>
            </>
          )}
        </div>
      </section>
    </main>
  );
}
