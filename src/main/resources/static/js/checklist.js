document.addEventListener("DOMContentLoaded", () => {
  const officeId = document.body.dataset.officeId;
  const boxes = [...document.querySelectorAll("#checklist input[type=checkbox]")];
  const key = `beforeYouVisit.checklist.${officeId}`;
  const progressText = document.getElementById("progressText");
  const progressBar = document.getElementById("progressBar");
  const saved = JSON.parse(localStorage.getItem(key) || "[]");

  boxes.forEach((box) => { box.checked = saved.includes(box.value); });
  const update = () => {
    const complete = boxes.filter((box) => box.checked).length;
    const percent = boxes.length ? Math.round((complete / boxes.length) * 100) : 0;
    progressText.textContent = `${complete} of ${boxes.length} ready`;
    progressBar.style.width = `${percent}%`;
    localStorage.setItem(key, JSON.stringify(boxes.filter((box) => box.checked).map((box) => box.value)));
  };
  boxes.forEach((box) => box.addEventListener("change", update));
  update();
});
