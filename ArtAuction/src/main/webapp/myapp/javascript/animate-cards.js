const cards = document.querySelectorAll(".animated-card");
cards.forEach((card) => {
  let bounds;

  function rotateToMouse(e) {
    const mouseX = e.clientX;
    const mouseY = e.clientY;
    const leftX = mouseX - bounds.x;
    const topY = mouseY - bounds.y;
    const center = {
      x: leftX - bounds.width / 2,
      y: topY - bounds.height / 2,
    };
    const distance = Math.sqrt(Math.pow(center.x, 2) + Math.pow(center.y, 2));

    card.style.transform = `
	    scale3d(1.05, 1.05, 1.05)
	    rotate3d(
	      ${center.y / 100},
	      ${-center.x / 100},
	      0,
	      ${Math.log(distance)}deg
	    )
	  `;

/*
    card.querySelector(".glow").style.backgroundImage = `
	    radial-gradient(
	      circle at
	      ${center.x * 2 + bounds.width / 2}px
	      ${center.y * 2 + bounds.height / 2}px,
	      #ffffff55,
	      #0000000f
	    )
	  `;
*/
  }

  card.addEventListener("mouseenter", () => {
    bounds = card.getBoundingClientRect();
    document.addEventListener("mousemove", rotateToMouse);
  });

  card.addEventListener("mouseleave", () => {
    document.removeEventListener("mousemove", rotateToMouse);
    card.style.transform = "";
    card.style.background = "";
  });
});
