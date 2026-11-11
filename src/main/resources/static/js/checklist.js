document.addEventListener("DOMContentLoaded", () => {
  const officeId = document.body.dataset.officeId;
  const boxes = [...document.querySelectorAll("#checklist input[type=checkbox]")];
  const key = `beforeYouVisit.checklist.${officeId}`;
  const progressText = document.getElementById("progressText");
  const progressBar = document.getElementById("progressBar");
  const checklistStatus = document.getElementById("checklistStatus");
  const saved = JSON.parse(localStorage.getItem(key) || "[]");

  boxes.forEach((box) => { box.checked = saved.includes(box.value); });
  const update = () => {
    const complete = boxes.filter((box) => box.checked).length;
    const percent = boxes.length ? Math.round((complete / boxes.length) * 100) : 0;
    progressText.textContent = `${complete} of ${boxes.length} ready`;
    progressBar.style.width = `${percent}%`;
    localStorage.setItem(key, JSON.stringify(boxes.filter((box) => box.checked).map((box) => box.value)));
    if (complete === boxes.length && boxes.length) {
      checklistStatus.textContent = "All required documents are marked ready. Your checklist is saved on this browser.";
    } else if (complete) {
      checklistStatus.textContent = `Saved: ${complete} document${complete === 1 ? "" : "s"} marked ready on this browser.`;
    } else {
      checklistStatus.textContent = "Nothing is marked ready yet. Your checklist is saved on this browser.";
    }
  };
  boxes.forEach((box) => box.addEventListener("change", update));
  update();
});
