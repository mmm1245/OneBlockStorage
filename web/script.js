socket = new WebSocket("ws://localhost:4321");

socket.onopen = function (e) {};

socket.onmessage = function (event) {
    let json = JSON.parse(event.data);
    let uuidId = "entry_" + json.uuid;
    let table = document.getElementById(uuidId);
    if (table == null) {
        table = document.createElement("table");
        table.id = uuidId;
        document.body.appendChild(table);
    }
    table.innerHTML = "";
    let headerTr = document.createElement("tr");
    let headerTh = document.createElement("th");
    headerTh.innerText = json.uuid;
    table.appendChild(headerTr);
    headerTr.appendChild(headerTh);
    for (let item of json.items) {
        if (item.name.startsWith("0x")) continue;
        let row = document.createElement("tr");
        let td = document.createElement("td");
        td.innerText = item.name;
        let btn = document.createElement("button");
        btn.innerText = "Request";
        btn.onclick = () => {
            socket.send(JSON.stringify({ uuid: json.uuid, slot: item.slot }));
        };
        row.appendChild(btn);
        row.appendChild(td);
        table.appendChild(row);
    }
};

socket.onclose = function (event) {
    alert("[close]");
};

socket.onerror = function (error) {
    alert(`[error]`);
};
