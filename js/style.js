function janken(player) {
    const hands = ["グー", "チョキ", "パー"];
    const computer = hands[Math.floor(Math.random() * hands.length)];

    let result = "";

    if (player === computer) {
        result = "あいこ！もう一回！";
    } else if (
        (player === "グー" && computer === "チョキ") ||
        (player === "チョキ" && computer === "パー") ||
        (player === "パー" && computer === "グー")
    ) {
        result = "あなたの勝ち！康兌準、負けました。";
    } else {
        result = "あなたの負け！康兌準の勝ちです。";
    }

    document.getElementById("player-hand").textContent = "あなた：" + player;
    document.getElementById("computer-hand").textContent = "康兌準：" + computer;
    document.getElementById("result").textContent = "結果：" + result;
}
let photoNumber = 0;

const photos = [
    "img/photo1.jpg",
    "img/photo2.jpg",
    "img/photo3.jpg"
];

function changePhoto() {
    photoNumber++;

    if (photoNumber >= photos.length) {
        photoNumber = 0;
    }

    document.getElementById("main-photo").src = photos[photoNumber];
}