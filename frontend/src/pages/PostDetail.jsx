import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "../css/PostDetail.css";
import api from "../api/client";
import { token } from "../auth/token";

export default function PostDetail() {
  const { postId } = useParams();
  const navigate = useNavigate();

  const composerRef = useRef(null);

  const [post, setPost] = useState(null);
  const [comments, setComments] = useState([]);

  const [loadingPost, setLoadingPost] = useState(false);
  const [loadingComments, setLoadingComments] = useState(false);

  const [errorPost, setErrorPost] = useState("");
  const [errorComments, setErrorComments] = useState("");

  const [commentInput, setCommentInput] = useState("");
  const [replyTo, setReplyTo] = useState(null);
  const [replyToName, setReplyToName] = useState("");

  const [submitting, setSubmitting] = useState(false);

  const [editingId, setEditingId] = useState(null);
  const [editInput, setEditInput] = useState("");

  const isLoggedIn = useMemo(() => {
    const at = token.getAccess();
    return (at != null) && (String(at).trim() !== "");
  }, []);

  const formatDate = (iso) => {
    if (!iso) return "";
    return iso.replace("T", " ").slice(0, 16);
  };

  const fetchPost = async () => {
    setLoadingPost(true);
    setErrorPost("");
    try {
      const res = await api.get(`/api/posts/${postId}`);
      setPost(res.data);
    } catch {
      setErrorPost("게시글을 불러오지 못했습니다.");
    } finally {
      setLoadingPost(false);
    }
  };

  const fetchComments = async () => {
    setLoadingComments(true);
    setErrorComments("");
    try {
      const res = await api.get(`/api/comments/posts/${postId}`);
      setComments(Array.isArray(res.data) ? res.data : []);
    } catch {
      setErrorComments("댓글을 불러오지 못했습니다.");
    } finally {
      setLoadingComments(false);
    }
  };

  useEffect(() => {
    if ((postId == null) || (String(postId).trim() === "")) {
      setErrorPost("게시글 ID가 필요합니다.");
      return;
    }
    fetchPost();
    fetchComments();
  }, [postId]);

  const onDeletePost = async () => {
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

  const resetComposer = () => {
    setCommentInput("");
    setReplyTo(null);
    setReplyToName("");
  };

  const focusComposer = () => {
    const el = composerRef.current;
    if (el) {
      el.scrollIntoView({ behavior: "smooth", block: "center" });
      setTimeout(() => {
        el.focus();
      }, 150);
    }
  };

  const onCreateComment = async (e) => {
    e.preventDefault();

    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }

    if ((commentInput == null) || (commentInput.trim() === "")) {
      alert("댓글 내용을 입력하세요.");
      return;
    }

    setSubmitting(true);
    try {
      await api.post(`/api/comments/posts/${postId}`, {
        content: commentInput.trim(),
        parentId: replyTo,
      });
      resetComposer();
      await fetchComments();
    } catch (e2) {
      alert(
        e2?.response?.data?.message ||
          e2?.message ||
          "댓글 작성에 실패했습니다."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const startReply = (comment) => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    setEditingId(null);
    setEditInput("");
    setReplyTo(comment.commentId);
    setReplyToName(comment.memberName || "");
    focusComposer();
  };

  const startEdit = (c) => {
    if (!isLoggedIn) {
      alert("로그인이 필요합니다.");
      navigate("/login");
      return;
    }
    setEditingId(c.commentId);
    setEditInput(c.content ?? "");
    setReplyTo(null);
    setReplyToName("");
  };

  const cancelEdit = () => {
    setEditingId(null);
    setEditInput("");
  };

  const onUpdateComment = async (commentId) => {
    if ((editInput == null) || (editInput.trim() === "")) {
      alert("댓글 내용을 입력하세요.");
      return;
    }

    setSubmitting(true);
    try {
      await api.put(`/api/comments/${commentId}`, {
        content: editInput.trim(),
      });
      cancelEdit();
      await fetchComments();
    } catch (e) {
      alert(
        e?.response?.data?.message ||
          e?.message ||
          "댓글 수정에 실패했습니다."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const onDeleteComment = async (commentId) => {
    if (!window.confirm("댓글을 삭제할까요?")) return;

    setSubmitting(true);
    try {
      await api.delete(`/api/comments/${commentId}`);
      if (editingId === commentId) {
        cancelEdit();
      }
      await fetchComments();
    } catch (e) {
      alert(
        e?.response?.data?.message ||
          e?.message ||
          "댓글 삭제에 실패했습니다."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const renderComment = (c, depth = 0) => {
    const isEditing = editingId === c.commentId;
    const hasChildren = Array.isArray(c.children) && c.children.length > 0;

    return (
      <li
        key={c.commentId}
        className={`comment-item ${depth > 0 ? "is-child" : ""}`}
      >
        <div className="comment-row">
          <div className="comment-main">
            <div className="comment-meta">
              <span className="comment-author">{c.memberName}</span>
              <span className="comment-dot">·</span>
              <span className="comment-date">{formatDate(c.createdAt)}</span>
              {c.updatedAt && c.updatedAt !== c.createdAt && (
                <>
                  <span className="comment-dot">·</span>
                  <span className="comment-edited">수정됨</span>
                </>
              )}
            </div>

            {!isEditing && <div className="comment-content">{c.content}</div>}

            {isEditing && (
              <div className="comment-editbox">
                <textarea
                  className="comment-textarea"
                  value={editInput}
                  onChange={(e) => setEditInput(e.target.value)}
                  rows={3}
                  disabled={submitting}
                />
                <div className="comment-actions">
                  <button
                    className="comment-btn ghost"
                    type="button"
                    onClick={cancelEdit}
                    disabled={submitting}
                  >
                    취소
                  </button>
                  <button
                    className="comment-btn"
                    type="button"
                    onClick={() => onUpdateComment(c.commentId)}
                    disabled={submitting}
                  >
                    저장
                  </button>
                </div>
              </div>
            )}
          </div>

          <div className="comment-side">
            <button
              className="comment-link"
              type="button"
              onClick={() => startReply(c)}
              disabled={submitting}
            >
              답글
            </button>

            {c.mine && (
              <>
                <button
                  className="comment-link"
                  type="button"
                  onClick={() => startEdit(c)}
                  disabled={submitting}
                >
                  수정
                </button>
                <button
                  className="comment-link danger"
                  type="button"
                  onClick={() => onDeleteComment(c.commentId)}
                  disabled={submitting}
                >
                  삭제
                </button>
              </>
            )}
          </div>
        </div>

        {hasChildren && (
          <ul className="comment-children">
            {c.children.map((ch) => renderComment(ch, depth + 1))}
          </ul>
        )}
      </li>
    );
  };

  return (
    <main className="postdetail-page">
      <section className="postdetail-shell">
        <button
          className="postdetail-back"
          type="button"
          onClick={() => navigate("/board")}
        >
          ← 목록
        </button>

        <div className="postdetail-card">
          {errorPost && <div className="postdetail-alert">{errorPost}</div>}
          {loadingPost && (
            <div className="postdetail-loading">불러오는 중...</div>
          )}

          {!loadingPost && !errorPost && post && (
            <>
              <h1 className="postdetail-title">{post.title}</h1>

              <div className="postdetail-meta">
                <span className="postdetail-author">{post.memberName}</span>
                <span className="postdetail-dot">·</span>
                <span className="postdetail-date">
                  {formatDate(post.createdAt)}
                </span>
                {post.updatedAt && post.updatedAt !== post.createdAt && (
                  <>
                    <span className="postdetail-dot">·</span>
                    <span className="postdetail-edited">수정됨</span>
                  </>
                )}
              </div>

              <div className="postdetail-content">{post.content}</div>

              <div className="postdetail-actions">
                <button
                  className="postdetail-btn ghost"
                  type="button"
                  onClick={() => navigate(`/posts/${postId}/edit`)}
                >
                  수정
                </button>
                <button
                  className="postdetail-btn danger"
                  type="button"
                  onClick={onDeletePost}
                >
                  삭제
                </button>
              </div>
            </>
          )}
        </div>

        <div className="comment-card" id="comment-composer">
          <div className="comment-head">
            <h3 className="comment-title">댓글</h3>

            {replyTo && (
              <div className="comment-replying">
                <span className="comment-replying-text">
                  {replyToName ? `${replyToName}님에게 답글 작성 중` : "답글 작성 중"}
                </span>
                <button
                  className="comment-cancel-reply"
                  type="button"
                  onClick={resetComposer}
                  disabled={submitting}
                >
                  취소
                </button>
              </div>
            )}
          </div>

          <form className="comment-form" onSubmit={onCreateComment}>
            <textarea
              ref={composerRef}
              className="comment-textarea"
              value={commentInput}
              onChange={(e) => setCommentInput(e.target.value)}
              placeholder={replyTo ? "답글을 입력하세요" : "댓글을 입력하세요"}
              rows={3}
              disabled={submitting}
            />
            <div className="comment-form-actions">
              {!isLoggedIn && (
                <button
                  className="comment-btn ghost"
                  type="button"
                  onClick={() => navigate("/login")}
                >
                  로그인
                </button>
              )}
              <button
                className="comment-btn"
                type="submit"
                disabled={submitting}
              >
                {submitting ? "처리 중..." : replyTo ? "답글 등록" : "등록"}
              </button>
            </div>
          </form>

          {errorComments && <div className="comment-alert">{errorComments}</div>}
          {loadingComments && (
            <div className="comment-loading">불러오는 중...</div>
          )}

          {!loadingComments && !errorComments && (
            <>
              {comments.length === 0 ? (
                <div className="comment-empty">댓글이 없습니다.</div>
              ) : (
                <ul className="comment-list">
                  {comments.map((c) => renderComment(c, 0))}
                </ul>
              )}
            </>
          )}
        </div>
      </section>
    </main>
  );
}
