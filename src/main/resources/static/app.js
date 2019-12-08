var stompClient = null;
var myName = null;

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
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        $("#helloFormId").show();
        stompClient.subscribe('/topic/greetings', function (greeting) {
            showGreeting(JSON.parse(greeting.body).content);
        });
        stompClient.subscribe('/topic/actions', function (greeting) {
            const newAction = JSON.parse(greeting.body);
            showAction(newAction.action, newAction.area);
        });
    });
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    myName = $("#name").val();
    stompClient.send("/app/hello", {}, JSON.stringify({'name': myName}));
    $("#helloFormId").hide();
    $("#actionFormId").show();
}
function sendAction(action, area) {
    stompClient.send("/app/do", {}, JSON.stringify({'action': action, 'area': area}));
}


function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

function showAction(action, area) {
    $("#greetings").append("<tr><td><i>" + myName + "</i> does <b>"+action+"</b> on area #"+area+"</td></tr>");
}


$(function () {
    $("#helloFormId").hide();
    $("#actionFormId").hide();


    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { sendName(); });
    $( ".actionHandler" ).click(function(){
        sendAction($(this).attr("act"), $(this).attr("area"));
    });
});