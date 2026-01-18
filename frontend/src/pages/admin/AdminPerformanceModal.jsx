import React from "react";

export default function AdminPerformanceModal({ isOpen, onClose, onSubmit, formData, setFormData }) {
  if (!isOpen) return null;

  const addSchedule = () => {
    setFormData({
      ...formData,
      schedules: [...formData.schedules, { startTime: "", endTime: "" }]
    });
  };

  const removeSchedule = (index) => {
    const newSchedules = formData.schedules.filter((_, i) => i !== index);
    setFormData({ ...formData, schedules: newSchedules });
  };

  const handleScheduleChange = (index, field, value) => {
    const newSchedules = [...formData.schedules];
    newSchedules[index][field] = value;
    setFormData({ ...formData, schedules: newSchedules });
  };

  return (
    <div className="perf-modal-overlay">
      <div className="perf-modal-window">
        <div className="perf-modal-header">
          <h3>{formData.performanceId ? "공연 정보 수정" : "신규 공연 등록"}</h3>
          <button className="perf-close-btn" onClick={onClose}>&times;</button>
        </div>

        <form className="perf-modal-form" onSubmit={(e) => { e.preventDefault(); onSubmit(); }}>
          <div className="perf-modal-body">
            <div className="perf-field">
              <label>공연명</label>
              <input type="text" value={formData.title} onChange={e => setFormData({...formData, title: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>공연 설명</label>
              <textarea value={formData.content} onChange={e => setFormData({...formData, content: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>장소</label>
              <input type="text" value={formData.location} onChange={e => setFormData({...formData, location: e.target.value})} required />
            </div>

            <div className="perf-field">
              <label>썸네일 URL</label>
              <div className="perf-url-row">
                <input type="text" value={formData.thumbnailUrl} onChange={e => setFormData({...formData, thumbnailUrl: e.target.value})} required />
                {formData.thumbnailUrl && <img src={formData.thumbnailUrl} alt="미리보기" className="perf-mini-preview" />}
              </div>
            </div>

            <div className="perf-schedule-box">
              <div className="perf-schedule-header">
                <label>공연 회차 설정</label>
                <button type="button" className="perf-add-btn" onClick={addSchedule}>+ 회차 추가</button>
              </div>
              <div className="perf-schedule-list">
                {formData.schedules.map((sc, index) => (
                  <div key={index} className="perf-schedule-row">
                    <input type="datetime-local" value={sc.startTime} onChange={e => handleScheduleChange(index, "startTime", e.target.value)} required />
                    <span className="perf-sep">~</span>
                    <input type="datetime-local" value={sc.endTime} onChange={e => handleScheduleChange(index, "endTime", e.target.value)} required />
                    <button type="button" className="perf-del-btn" onClick={() => removeSchedule(index)}>&times;</button>
                  </div>
                ))}
              </div>
            </div>

            <div className="perf-row">
              <div className="perf-field">
                <label>티켓 가격</label>
                <input type="number" value={formData.price} onChange={e => setFormData({...formData, price: e.target.value})} required />
              </div>
              <div className="perf-field">
                <label>상태</label>
                <select value={formData.status} onChange={e => setFormData({...formData, status: e.target.value})}>
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