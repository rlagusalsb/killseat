import { useEffect, useState } from "react";
import api from "../../api/client";
import "../../css/admin/AdminSeatModal.css";

export default function AdminSeatModal({ isOpen, onClose, scheduleId, performanceTitle, scheduleTime }) {
  const [seats, setSeats] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (isOpen && scheduleId) fetchSeats();
  }, [isOpen, scheduleId]);

  const fetchSeats = () => {
    setLoading(true);
    api.get(`/api/admin/performance-seats/performance/${scheduleId}`)
       .then(res => {
         const data = res.data.content || res.data;
         const sortedData = Array.isArray(data) 
            ? data.sort((a, b) => a.seatNumber.localeCompare(b.seatNumber, undefined, {numeric: true})) 
            : [];
         setSeats(sortedData);
         setLoading(false);
       })
       .catch(err => {
         console.error(err);
         setLoading(false);
       });
  };

  const handleToggleBlock = (seatId, currentStatus) => {
    if (currentStatus === "BOOKED" || currentStatus === "HOLD") {
      alert("상태를 변경할 수 없는 좌석입니다.");
      return;
    }
    const isBlocked = currentStatus === "BLOCKED";
    const endpoint = `/api/admin/performance-seats/${seatId}/${isBlocked ? 'unblock' : 'block'}`;
    
    if (window.confirm(isBlocked ? "판매 가능 상태로 변경하시겠습니까?" : "좌석을 차단하시겠습니까?")) {
      api.patch(endpoint).then(() => fetchSeats()).catch(() => alert("변경 실패"));
    }
  };

  const groupedSeats = seats.reduce((acc, seat) => {
    const row = seat.seatNumber.charAt(0);
    if (!acc[row]) acc[row] = [];
    acc[row].push(seat);
    return acc;
  }, {});

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="seat-modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div className="header-info">
            <span className="modal-sub">SEAT MANAGEMENT</span>
            <div className="title-row">
              <h4 className="modal-title">{performanceTitle}</h4>
              <span className="schedule-badge">{scheduleTime}</span>
            </div>
          </div>
          <button className="close-x" onClick={onClose}>&times;</button>
        </div>

        {loading ? (
          <div className="loading-container">데이터를 불러오는 중입니다...</div>
        ) : (
          <div className="seat-container">
            <div className="stage-area">STAGE</div>
            <div className="rows-wrapper">
              {Object.keys(groupedSeats).sort().map(row => (
                <div key={row} className="seat-row">
                  <div className="row-label">{row}열</div>
                  <div className="row-seats">
                    {groupedSeats[row].map(seat => (
                      <div 
                        key={seat.performanceSeatId} 
                        className={`seat-box ${seat.status}`}
                        onClick={() => handleToggleBlock(seat.performanceSeatId, seat.status)}
                      >
                        {seat.seatNumber}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
            <div className="seat-legend">
              <div className="legend-item"><span className="box AVAILABLE"></span>판매가능</div>
              <div className="legend-item"><span className="box BLOCKED"></span>판매차단</div>
              <div className="legend-item"><span className="box BOOKED"></span>예매완료</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}