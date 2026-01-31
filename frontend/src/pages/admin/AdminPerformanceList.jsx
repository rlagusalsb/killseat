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
         setPerformances(res.data.content); 
         setPageInfo({
           currentPage: res.data.currentPage,
           totalPages: res.data.totalPages,
           totalElements: res.data.totalElements
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
    const datePart = sc.startTime.substring(5, 10).replace("-", "/");
    const timePart = sc.startTime.substring(11, 16);
    setSelectedDateTime(`${datePart} ${timePart}`);
    setIsSeatModalOpen(true);
  };

  const handleOpenModal = (pf = null) => {
    if (pf) {
      setFormData({ ...pf, schedules: pf.schedules || [] });
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

  return (
    <div className="admin-list-container">
      <div className="list-header">
        <h3>공연 및 회차 관리 ({pageInfo.totalElements})</h3>
        <button className="btn-create" onClick={() => handleOpenModal()}>공연 등록</button>
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
          {performances.map(pf => (
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
          ))}
        </tbody>
      </table>

      <div className="pagination">
        <button 
          onClick={() => handlePageChange(pageInfo.currentPage - 1)}
          disabled={pageInfo.currentPage === 0}
        >
          이전
        </button>
        
        {[...Array(pageInfo.totalPages)].map((_, i) => (
          <button 
            key={i} 
            className={pageInfo.currentPage === i ? "active" : ""}
            onClick={() => handlePageChange(i)}
          >
            {i + 1}
          </button>
        ))}

        <button 
          onClick={() => handlePageChange(pageInfo.currentPage + 1)}
          disabled={pageInfo.currentPage >= pageInfo.totalPages - 1}
        >
          다음
        </button>
      </div>

      <AdminPerformanceModal 
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onSubmit={handleSubmit}
        formData={formData}
        setFormData={setFormData}
      />

      <AdminSeatModal 
        isOpen={isSeatModalOpen}
        onClose={() => setIsSeatModalOpen(false)}
        scheduleId={selectedScheduleId}
        performanceTitle={selectedTitle}
        scheduleTime={selectedDateTime}
      />
    </div>
  );
}