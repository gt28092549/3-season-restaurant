let basePrice = 0;
let sizeExtraPrice = 0;
let meatExtraPrice = 0;
let quantity = 1;
let foodModal;

// ตัวแปรสำหรับเก็บข้อมูลเมนูที่เปิดอยู่
let currentItem = {
    name: '',
    price: 0,
    imageUrl: '',
    options: ''
};

document.addEventListener("DOMContentLoaded", function () {
    const modalElement = document.getElementById('foodOptionModal');
    if (modalElement) {
        foodModal = new bootstrap.Modal(modalElement);
    }
});

function openFoodModal(name, description, price, imageUrl, type) {
    basePrice = price;
    quantity = 1;
    sizeExtraPrice = 0;
    meatExtraPrice = 0;

    // บันทึกข้อมูลเมนูปัจจุบัน
    currentItem.name = name;
    currentItem.price = price;
    currentItem.imageUrl = imageUrl;

    document.getElementById('modalFoodTitle').innerText = name;
    document.getElementById('modalFoodDesc').innerText = description;
    document.getElementById('modalHeroBg').style.backgroundImage = `url('${imageUrl}')`;
    document.getElementById('qtyText').innerText = quantity;

    document.getElementById('foodOptionsSection').style.display = 'none';
    document.getElementById('drinkOptionsSection').style.display = 'none';
    document.getElementById('soupOptionsSection').style.display = 'none';

    if (type === 'FOOD') {
        document.getElementById('foodOptionsSection').style.display = 'block';
        resetFoodSelections();
    } else if (type === 'DRINK') {
        document.getElementById('drinkOptionsSection').style.display = 'block';
        resetDrinkSelections();
    } else if (type === 'SOUP') {
        document.getElementById('soupOptionsSection').style.display = 'block';
        resetSoupSelections();
    }

    updateTotalPrice();
    if (foodModal) {
        foodModal.show();
    }
}

function resetFoodSelections() {
    document.querySelectorAll('#foodOptionsSection .option-box').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
    document.querySelectorAll('#meatOptions .btn-sweetness').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
}

function resetDrinkSelections() {
    document.querySelectorAll('#drinkOptionsSection .option-box').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
    document.querySelectorAll('#sweetnessOptions .btn-sweetness').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
}

function resetSoupSelections() {
    document.querySelectorAll('#soupOptionsSection .option-box').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
    document.querySelectorAll('#spicyOptions .btn-sweetness').forEach((el, index) => {
        el.classList.toggle('selected', index === 0);
    });
}

function selectSize(element, extraPrice) {
    const parentSection = element.closest('.mb-3');
    parentSection.querySelectorAll('.option-box').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    sizeExtraPrice = extraPrice;
    updateTotalPrice();
}

function selectMeat(element, extraPrice) {
    document.querySelectorAll('#meatOptions .btn-sweetness').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
    meatExtraPrice = extraPrice;
    updateTotalPrice();
}

function selectSpicy(element) {
    document.querySelectorAll('#spicyOptions .btn-sweetness').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
}

function selectSweetness(element) {
    document.querySelectorAll('#sweetnessOptions .btn-sweetness').forEach(el => el.classList.remove('selected'));
    element.classList.add('selected');
}

function changeQty(change) {
    quantity += change;
    if (quantity < 1) quantity = 1;
    document.getElementById('qtyText').innerText = quantity;
    updateTotalPrice();
}

function updateTotalPrice() {
    let total = (basePrice + sizeExtraPrice + meatExtraPrice) * quantity;
    document.getElementById('totalPriceText').innerText = '฿' + total;
}

// 🛒 บันทึกรายการลง LocalStorage แล้วเปิดหน้าสถานะครัว
function addToCart() {
    let activeOrders = JSON.parse(localStorage.getItem('myOrders')) || [];

    let calculatedPrice = (basePrice + sizeExtraPrice + meatExtraPrice) * quantity;
    
    let newOrder = {
        id: Date.now(),
        name: currentItem.name,
        price: calculatedPrice,
        quantity: quantity,
        imageUrl: currentItem.imageUrl,
        status: 'กำลังปรุง',
        time: new Date().toLocaleTimeString('th-TH', { hour: '2-digit', minute: '2-digit' })
    };

    activeOrders.push(newOrder);
    localStorage.setItem('myOrders', JSON.stringify(activeOrders));

    if (foodModal) {
        foodModal.hide();
    }
    window.location.href = "/order-status";

    document.addEventListener("DOMContentLoaded", function () {
    // โหลด Modal เมนูอาหาร
    const modalElement = document.getElementById('foodOptionModal');
    if (modalElement) {
        foodModal = new bootstrap.Modal(modalElement);
    }

    // 👤 ดึงชื่อผู้ใช้ที่สมัคร/ล็อกอินมาแสดงผลตรง Header
    updateHeaderUserName();
});

function updateHeaderUserName() {
    const savedName = localStorage.getItem('userName');
    const isLoggedIn = localStorage.getItem('isLoggedIn');
    const headerNameElement = document.getElementById('headerUserName');

    if (headerNameElement) {
        if (isLoggedIn === 'true' && savedName) {
            headerNameElement.innerText = savedName;
        } else {
            headerNameElement.innerText = "3 Seaso...";
        }
    }
}
}