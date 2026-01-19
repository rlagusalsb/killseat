import React from "react";

export default function AdminPerformanceModal({ isOpen, onClose, onSubmit, formData, setFormData }) {
  if (!isOpen) return null;

  const addSchedule = () => {
    setFormData((prev) => ({
      ...prev,
      schedules: [...(prev.schedules || []), { startTime: "", endTime: "" }]
    }));
  };

  const removeSchedule = (index) => {
    setFormData((prev) => ({
      ...prev,
      schedules: prev.schedules.filter((_, i) => i !== index)
    }));
  };

  const handleScheduleChange = (index, field, value) => {
    setFormData((prev) => {
      const newSchedules = prev.schedules.map((sc, i) => {
        if (i === index) return { ...sc, [field]: value };
        return sc;
      });
      return { ...prev, schedules: newSchedules };
    });
  };

  const formatToLocalDateTime = (dateTimeStr) => {
    if (!dateTimeStr) return "";
    let val = dateTimeStr.replace(" ", "T");
    if (val.length === 16) val += ":00";
    return val;
  };

  const handleFormSubmit = (e) => {
    e.preventDefault();
    const payload = {
      ...formData,
      price: Number(formData.price),
      schedules: (formData.schedules || []).map(sc => ({
        ...sc,
        startTime: formatToLocalDateTime(sc.startTime),
        endTime: formatToLocalDateTime(sc.endTime)
      }))
    };
    onSubmit(payload);
  };

  return (
    <div className="perf-modal-overlay">
      <div className="perf-modal-window">
        <div className="perf-modal-header">
          <h3>{formData.performanceId ? "공연 정보 수정" : "신규 공연 등록"}</h3>
          <button className="perf-close-btn" type="button" onClick={onClose}>&times;</button>
        </div>

        <form className="perf-modal-form" onSubmit={handleFormSubmit}>
          <div className="perf-modal-body">
            <div className="perf-field">
              <label>공연명</label>
              <input type="text" value={formData.title || ""} onChange={e => setFormData({...formData, title: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>공연 설명</label>
              <textarea value={formData.content || ""} onChange={e => setFormData({...formData, content: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>장소</label>
              <input type="text" value={formData.location || ""} onChange={e => setFormData({...formData, location: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>썸네일 URL</label>
              <div className="perf-url-row">
                <input type="text" value={formData.thumbnailUrl || ""} onChange={e => setFormData({...formData, thumbnailUrl: e.target.value})} required />
                <div className="perf-mini-preview-container" style={{ background: '#f4f4f4', width: '60px', height: '40px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '4px', overflow: 'hidden' }}>
                  <img 
                    src={formData.thumbnailUrl || "https://www.gstatic.com/webp/gallery/1.sm.jpg"} 
                    alt="미리보기" 
                    className="perf-mini-preview" 
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    onError={(e) => {
                      e.target.onerror = null;
                      e.target.src = "https://www.gstatic.com/webp/gallery/1.sm.jpg";
                    }} 
                  />
                </div>
              </div>
            </div>

            <div className="perf-schedule-box">
              <div className="perf-schedule-header">
                <label>공연 회차 설정</label>
                <button type="button" className="perf-add-btn" onClick={addSchedule}>+ 회차 추가</button>
              </div>
              <div className="perf-schedule-list">
                {formData.schedules && formData.schedules.map((sc, index) => (
                  <div key={index} className="perf-schedule-row">
                    <input 
                      type="datetime-local" 
                      value={sc.startTime ? sc.startTime.substring(0, 16) : ""} 
                      onChange={e => handleScheduleChange(index, "startTime", e.target.value)} 
                      required 
                    />
                    <span className="perf-sep">~</span>
                    <input 
                      type="datetime-local" 
                      value={sc.endTime ? sc.endTime.substring(0, 16) : ""} 
                      onChange={e => handleScheduleChange(index, "endTime", e.target.value)} 
                      required 
                    />
                    <button type="button" className="perf-del-btn" onClick={() => removeSchedule(index)}>&times;</button>
                  </div>
                ))}
              </div>
            </div>

            <div className="perf-row">
              <div className="perf-field">
                <label>티켓 가격</label>
                <input type="number" value={formData.price || ""} onChange={e => setFormData({...formData, price: e.target.value})} required />
              </div>
              <div className="perf-field">
                <label>상태</label>
                <select value={formData.status || "BEFORE_OPEN"} onChange={e => setFormData({...formData, status: e.target.value})}>
                  <option value="BEFORE_OPEN">공연 예정</option>
                  <option value="OPEN">판매 중</option>
                  <option value="CLOSED">판매 종료</option>
                </select>
              </div>
            </div>
          </div>

          <div className="perf-modal-footer">
            <button type="button" className="perf-cancel-btn" onClick={onClose}>취소</button>
            <button type="submit" className="perf-save-btn">저장하기</button>
          </div>
        </form>
      </div>
    </div>
  );
}