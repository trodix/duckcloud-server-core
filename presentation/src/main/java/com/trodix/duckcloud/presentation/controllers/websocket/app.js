const BEARER = `eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIwSGtXR1ozWl84TGd5el9QY09WMGZkVElERTFmZS1NWGw0YUJmQ3plX2dFIn0.eyJleHAiOjE3MTg1NjQ2MjEsImlhdCI6MTcxODU2MjgyMSwianRpIjoiZjI3ZGQ3YTEtZjMyMC00YmYyLWJhZDgtZjA4ZjkyY2U0MWY2IiwiaXNzIjoiaHR0cDovL3Ryb2RpeC5sb2NhbDo4MDgwL3JlYWxtcy9tYXJrZXQiLCJhdWQiOiJhY2NvdW50Iiwic3ViIjoiYjMwODg1ODYtNzZmMi00YTExLTkxMjAtMTY4NGZjN2QyZDAxIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoicXVhY2stZHVjay1lY20tdWkiLCJzZXNzaW9uX3N0YXRlIjoiYzlmOGMwMDUtMjMyYi00NWRiLTlmZWMtMzExOWQxNzg1ODQ5IiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIqIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJkZWZhdWx0LXJvbGVzLW1hcmtldCIsIm9mZmxpbmVfYWNjZXNzIiwidW1hX2F1dGhvcml6YXRpb24iXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiXX19LCJzY29wZSI6Im9wZW5pZCBlbWFpbCBvZmZsaW5lX2FjY2VzcyBwcm9maWxlIiwic2lkIjoiYzlmOGMwMDUtMjMyYi00NWRiLTlmZWMtMzExOWQxNzg1ODQ5IiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJuYW1lIjoiVXNlcjMgQmFyMyIsInByZWZlcnJlZF91c2VybmFtZSI6InVzZXIzIiwiZ2l2ZW5fbmFtZSI6IlVzZXIzIiwiZmFtaWx5X25hbWUiOiJCYXIzIiwiZW1haWwiOiJ1c2VyM0B0cm9kaXguY29tIn0.csAKKSpEUe9VRBq28n2V2IBdmr-PtSLVS7XoyoMjQHZm7NUwJXb9EZWj24IJgtlkAolJMXjHzsEM8jIpKh6WSRoeaW-OZbAqLUggUuxeqEoxyC0qT2G2_NxD6niQnii5VI_rQKaPnjL0IyPfulV7mI7dY2lihlfR21OozmkK91H_bLlnf0YLbvtBEV7BOBcHGBXDYwLRaOpYxRg0SBmQI8NXR_Jlnz2quHPHA4njZyjbSqkGA5zpR0TEHog3wXsfwEWAN1_dhJuwCUxma6JsRXEmVv7xByrXJK8g0AhuQ0vVMzB9ydzQSmtAWqisb8y29YZ5UTFkkn8cwJ7PvrFl5g`
const decodedToken = jwtDecode(BEARER);
console.log(decodedToken);

const stompClient = new StompJs.Client({
    brokerURL: 'ws://localhost:8010/ws',
    connectHeaders: {
        'Authorization': `Bearer ${BEARER}`
    },
    debug: (str) => {
        console.log(str);
    },
});

stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);

    stompClient.subscribe('/topic/greetings', (response) => {
        console.log("/topic/greetings greeting: ", response.body);
        showGreeting(JSON.parse(response.body).name);
    });

    const queue = `/user/${decodedToken['sub']}/queue/notifications`;
    stompClient.subscribe(queue, (response) => {
        console.log(queue, response.body);
        showGreeting(JSON.parse(response.body).name);
    });

};

stompClient.onWebSocketError = (error) => {
    console.error('Error with websocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html("");
}

function connect() {
    stompClient.activate();
}

function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    stompClient.publish({
        destination: "/app/hello",
        body: JSON.stringify({'name': $("#name").val()})
    });
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', (e) => e.preventDefault());
    $( "#connect" ).click(() => connect());
    $( "#disconnect" ).click(() => disconnect());
    $( "#send" ).click(() => sendName());
});

async function getToken() {
    return fetch('http://trodix.local:8080/realms/market/protocol/openid-connect/token', {
        method: 'POST',
        headers:{
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: new URLSearchParams({
            'client_id': 'quack-duck-ecm-ui',
            'grant_type': 'password',
            'username': 'user3',
            'password': 'user3'
        })
    });
}