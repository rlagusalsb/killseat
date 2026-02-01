import { useEffect, useState } from "react";
import api from "../../api/client";
import AdminPerformanceModal from "./AdminPerformanceModal";
import AdminSeatModal from "./AdminSeatModal";
import "../../css/admin/AdminPerformanceList.css";

export default function AdminPerformanceList() {
  const [performances, setPerformances] = useState([]);
  const [pageInfo, setPageInfo] = useState({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0
  });

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isSeatModalOpen, setIsSeatModalOpen] = useState(false);
  const [selectedScheduleId, setSelectedScheduleId] = useState(null);
  const [selectedTitle, setSelectedTitle] = useState("");
  const [selectedDateTime, setSelectedDateTime] = useState("");
  
  const [formData, setFormData] = useState({
    performanceId: "", title: "", content: "", location: "",
    price: 0, thumbnailUrl: "", status: "BEFORE_OPEN", schedules: []
  });

  useEffect(() => {
    fetchPerformances(pageInfo.currentPage);
  }, [pageInfo.currentPage]);

  const fetchPerformances = (page = 0) => {
    api.get(`/api/admin/performances?page=${page}`)
       .then(res => {
         setPerformances(res.data.content || []); 
         setPageInfo({
           currentPage: res.data.pageNumber || 0, 
           totalPages: res.data.totalPages || 0,
           totalElements: res.data.totalElements || 0
         });
       })
       .catch(err => console.error(err));
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pageInfo.totalPages) {
      setPageInfo(prev => ({ ...prev, currentPage: newPage }));
    }
  };

  const handleSeatManage = (pf, sc) => {
    if (!sc?.scheduleId) {
      alert("회차 ID가 없습니다.");
      return;
    }
    setSelectedTitle(pf.title);
    setSelectedScheduleId(sc.scheduleId);
    const datePart = sc.startTime ? sc.startTime.substring(5, 10).replace("-", "/") : "";
    const timePart = sc.startTime ? sc.startTime.substring(11, 16) : "";
    setSelectedDateTime(`${datePart} ${timePart}`);
    setIsSeatModalOpen(true);
  };

  const handleOpenModal = (pf = null) => {
    if (pf) {
      setFormData({
        performanceId: pf.performanceId || "",
        title: pf.title || "",
        content: pf.content || "",
        location: pf.location || "",
        price: pf.price || 0,
        thumbnailUrl: pf.thumbnailUrl || "",
        status: pf.status || "BEFORE_OPEN",
        schedules: pf.schedules || []
      });
    } else {
      setFormData({
        performanceId: "", title: "", content: "", location: "",
        price: 0, thumbnailUrl: "", status: "BEFORE_OPEN",
        schedules: [{ startTime: "", endTime: "" }]
      });
    }
    setIsModalOpen(true);
  };

  const handleSubmit = () => {
    const request = formData.performanceId 
      ? api.put(`/api/admin/performances/${formData.performanceId}`, formData)
      : api.post("/api/admin/performances", formData);

    request.then(() => {
      setIsModalOpen(false);
      fetchPerformances(pageInfo.currentPage);
    }).catch(err => {
      alert("저장 실패: " + (err.response?.data?.message || "서버 에러"));
    });
  };

  const renderPagination = () => {
    const { currentPage, totalPages } = pageInfo;
    if (totalPages <= 1) return null;

    const blockSize = 5;
    const currentBlock = Math.floor(currentPage / blockSize);
    const startPage = currentBlock * blockSize;
    const endPage = Math.min(startPage + blockSize, totalPages);

    const pages = [];
    for (let i = startPage; i < endPage; i++) {
      pages.push(
        <button 
          key={i} 
          className={currentPage === i ? "active" : ""}
          onClick={() => handlePageChange(i)}
        >
          {i + 1}
        </button>
      );
    }

    return (
      <div className="pagination">
        <button 
          onClick={() => handlePageChange(startPage - 1)}
          disabled={startPage === 0}
        >
          이전
        </button>
        {pages}
        <button 
          onClick={() => handlePageChange(endPage)}
          disabled={endPage >= totalPages}
        >
          다음
        </button>
      </div>
    );
  };

  return (
    <div className="admin-list-container">
      <div className="list-header">
        <h3>공연 및 회차 관리 ({pageInfo.totalElements})</h3>
        <button className="btn-create" onClick={() => handleOpenModal(null)}>공연 등록</button>
      </div>

      <table className="admin-table">
        <thead>
          <tr>
            <th style={{ width: "60px" }}>ID</th>
            <th>공연명</th>
            <th style={{ width: "120px" }}>장소</th>
            <th style={{ width: "250px" }}>회차 선택 (좌석관리)</th>
            <th style={{ width: "100px" }}>상태</th>
            <th style={{ width: "100px" }}>액션</th>
          </tr>
        </thead>
        <tbody>
          {performances.length > 0 ? (
            performances.map(pf => (
              <tr key={pf.performanceId}>
                <td>{pf.performanceId}</td>
                <td className="pf-title">{pf.title}</td>
                <td>{pf.location}</td>
                <td>
                  <div className="schedule-tags">
                    {pf.schedules?.map((sc, idx) => (
                      <button 
                        key={idx} 
                        className="time-tag-btn" 
                        onClick={() => handleSeatManage(pf, sc)}
                      >
                        {sc.startTime?.substring(5, 10).replace("-", "/")} {sc.startTime?.substring(11, 16)}
                      </button>
                    ))}
                  </div>
                </td>
                <td><span className={`status-badge ${pf.status}`}>{pf.status}</span></td>
                <td>
                  <div className="action-btns">
                    <button className="btn-edit" onClick={() => handleOpenModal(pf)}>수정</button>
                  </div>
                </td>
              </tr>
            ))
          ) : (
            <tr><td colSpan="6" style={{textAlign:'center', padding:'40px'}}>데이터가 없습니다.</td></tr>
          )}
        </tbody>
      </table>

      {renderPagination()}

      {isModalOpen && (
        <AdminPerformanceModal 
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          onSubmit={handleSubmit}
          formData={formData}
          setFormData={setFormData}
        />
      )}

      {isSeatModalOpen && (
        <AdminSeatModal 
          isOpen={isSeatModalOpen}
          onClose={() => setIsSeatModalOpen(false)}
          scheduleId={selectedScheduleId}
          performanceTitle={selectedTitle}
          scheduleTime={selectedDateTime}
        />
      )}
    </div>
  );
}