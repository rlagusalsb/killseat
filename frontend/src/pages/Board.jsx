import { useEffect, useState } from "react";
import "../css/Board.css";
import api from "../api/client";
import { useNavigate } from "react-router-dom";

export default function Board() {
  const navigate = useNavigate();

  const [posts, setPosts] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [keyword, setKeyword] = useState("");
  const [input, setInput] = useState("");

  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);

  const size = 10;

  const fetchPosts = async () => {
    setLoading(true);
    setFailed(false);

    try {
      const params = { page, size };

      if ((keyword != null) && (keyword.trim() !== "")) {
        params.keyword = keyword.trim();
      }

      const res = await api.get("/api/posts", { params });

      setPosts(res.data.content || []);
      setTotalPages(res.data.totalPages ?? 0);
    } catch {
      setFailed(true);
      setPosts([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPosts();
  }, [page, keyword]);

  const onSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    setKeyword(input);
  };

  const onPrev = () => {
    setPage((p) => Math.max(0, p - 1));
  };

  const onNext = () => {
    setPage((p) => Math.min(Math.max(0, totalPages - 1), p + 1));
  };

  const onGoWrite = () => {
    navigate("/posts/write");
  };

  const onClickPost = (postId) => {
    navigate(`/posts/${postId}`);
  };

  const formatDate = (iso) => {
    if (!iso) return "";
    return iso.replace("T", " ").slice(0, 16);
  };

  return (
    <main className="board-page">
      <section className="board-shell">
        <header className="board-header">
          <div className="board-heading">
            <h1 className="board-title">커뮤니티</h1>
            <p className="board-desc">
              공연 예약, 공연 후기, 좌석 시야 등 자유롭게 공유해요
            </p>
          </div>

          <div className="board-actions">
            <form className="board-search" onSubmit={onSearchSubmit}>
              <span className="board-search-icon">⌕</span>
              <input
                className="board-search-input"
                value={input}
                onChange={(e) => setInput(e.target.value)}
                placeholder="키워드로 검색"
              />
              <button className="board-search-btn" type="submit">
                검색
              </button>
            </form>
          </div>
        </header>

        <div className="board-card">
          <div className="board-toolbar">
            <span className="board-count">
              {keyword && keyword.trim() !== "" ? (
                <>
                  검색: <strong>{keyword.trim()}</strong>
                </>
              ) : (
                <>전체</>
              )}
            </span>

            <button className="board-write-btn" type="button" onClick={onGoWrite}>
              글쓰기
            </button>
          </div>

          {failed && !loading && (
            <div className="board-notice">
              게시글을 불러오지 못했습니다.
              <button className="board-retry" type="button" onClick={fetchPosts}>
                다시 시도
              </button>
            </div>
          )}

          {loading && <div className="board-loading">불러오는 중...</div>}

          {!loading && !failed && posts.length === 0 && (
            <div className="board-empty">
              <div className="board-empty-icon">📝</div>
              <h3>아직 작성된 글이 없어요</h3>
              <p>첫 글을 작성해서 커뮤니티를 시작해보세요.</p>
              <button className="board-empty-btn" type="button" onClick={onGoWrite}>
                첫 글 작성하기
              </button>
            </div>
          )}

          {!loading && !failed && posts.length > 0 && (
            <ul className="board-list">
              {posts.map((post) => (
                <li
                  key={post.postId}
                  className="board-item is-row"
                  role="button"
                  tabIndex={0}
                  onClick={() => onClickPost(post.postId)}
                  onKeyDown={(e) => {
                    if ((e.key === "Enter") || (e.key === " ")) {
                      onClickPost(post.postId);
                    }
                  }}
                >
                  <div className="board-item-main">
                    <h3 className="board-item-title">{post.title}</h3>
                  </div>

                  <div className="board-item-meta">
                    <span className="board-item-user">{post.memberName}</span>
                    <span className="board-item-date">{formatDate(post.createdAt)}</span>
                  </div>
                </li>
              ))}
            </ul>
          )}

          <footer className="board-footer">
            <button
              className="board-page-btn"
              type="button"
              onClick={onPrev}
              disabled={page === 0}
            >
              이전
            </button>

            <span className="board-page-state">
              {totalPages === 0 ? "0 / 0" : `${page + 1} / ${totalPages}`}
            </span>

            <button
              className="board-page-btn"
              type="button"
              onClick={onNext}
              disabled={(totalPages === 0) || (page + 1 >= totalPages)}
            >
              다음
            </button>
          </footer>
        </div>
      </section>
    </main>
  );
}
