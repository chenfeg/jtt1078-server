let ws;
let deviceId;
let uuid;
const stopPlayFlag = [];
let command;

function createUUID() {
    function s4() {
        return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
    }

    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

function getFormatDateTime(date) {
    const year = date.getFullYear(); //返回指定日期的年份
    const month = repair(date.getMonth() + 1);//月
    const day = repair(date.getDate());//日
    const hour = repair(date.getHours());//时
    const minute = repair(date.getMinutes());//分
    const second = repair(date.getSeconds());//秒
    //当前时间
    return year + "-" + month + "-" + day
        + " " + hour + ":" + minute + ":" + second;
}

//补0
function repair(i){
    if (i >= 0 && i <= 9) {
        return "0" + i;
    } else {
        return i;
    }
}

function getChannelId(num) {
    let channelIdStr = 'channel';
    if (num < 10) {
        channelIdStr = channelIdStr + '0' + num;
    } else {
        channelIdStr = channelIdStr + num;
    }
    return channelIdStr;
}

function createPlayVideoBlock(videoElementId) {
    const liveChannelUL = document.getElementById('liveChannelList');
    const liStrPrefix = '<video id="';
    const liStrSuffix =
        '" style="object-fit: fill; width: 100%; height = 100%;" controls autoplay width="100%" height="100%" poster=""></video>';
    const sign = '<span>通道 ' + videoElementId + '</span>';
    const liElement = document.createElement('li');
    liElement.innerHTML = sign + liStrPrefix + videoElementId + liStrSuffix;
    liveChannelUL.appendChild(liElement);
}

function responseAction(data) {
    const status = data.status;
    if (status !== 0) {
        alert(data.message);
        console.error('WebSocket服务端响应出错，', data);
        return;
    }
    alert('指令响应成功');
}

// 控制播放历史音视频请求
function historyControlRequest() {
    const deviceId = document.getElementById('deviceId_his').value;
    const channelId = Number(document.getElementById('channelId_his').value);
    const selectPlaybackCommand = document.getElementById('playbackCommand_his');
    const selectPlaybackCommandIndex = selectPlaybackCommand.selectedIndex;
    const selectPlaybackSpeed = document.getElementById('playbackSpeed_his');
    const selectPlaybackSpeedIndex = selectPlaybackSpeed.selectedIndex;
    const command = Number(selectPlaybackCommand.options[selectPlaybackCommandIndex].value);

    const paramsJson = {
        'deviceId': deviceId, //设备编号
        'channel': channelId, //通道号
        'command': command, // 回放指令
        'playBackRatio': Number(selectPlaybackSpeed.options[selectPlaybackSpeedIndex].value), //回放速度
        'fastPosition': document.getElementById('dragPalyPosition_his').value,
    };
    const reqObj = {
        'action': 'ControlHist',
        'uuid': uuid,
        'params': paramsJson,
    };
    sendMessage(reqObj);
    if (command === 2) {
        stopPlayFlag[channelId] = true;
    }
}

// 历史音视频播放请求
function playHistoryRequest() {
    const deviceId = document.getElementById('deviceId_his').value;
    const channelId = document.getElementById('channelId_his').value;
    const selectVideoType = document.getElementById('videoType_his');
    const selectVideoTypeIndex = selectVideoType.selectedIndex;
    const selectStreamType = document.getElementById('streamType_his');
    const selectStreamTypeIndex = selectStreamType.selectedIndex;
    const selectMemoryType = document.getElementById('memoryType_his');
    const selectMemoryTypeIndex = selectMemoryType.selectedIndex;
    const selectPlaybackMethod = document.getElementById('playbackMethod_his');
    const selectPlaybackMethodIndex = selectPlaybackMethod.selectedIndex;
    const selectPlaySpeed = document.getElementById('playSpeed_his');
    const selectPlaySpeedIndex = selectPlaySpeed.selectedIndex;
    const startTime = document.getElementById('startTime_his').value;
    const endTime = document.getElementById('endTime_his').value;
    const audio = {
        'format': "G711", //格式
        'sampleRate': 8000, //采样率
        'bit': 16, //位数
        'channel': 1 //通道数
    }
    const video = {
        'format': 'h264', //格式
        'frameRate': 25 //帧率
    }
    const paramsJson = {
        'deviceId': deviceId,
        'channel': Number(channelId), //逻级通道
        'mediaType': Number(selectVideoType.options[selectVideoTypeIndex].value), //媒体类型
        'streamType': Number(selectStreamType.options[selectStreamTypeIndex].value), //码流类型
        'memoryType': Number(selectMemoryType.options[selectMemoryTypeIndex].value), //存储器类型
        'playBackType': Number(selectPlaybackMethod.options[selectPlaybackMethodIndex].value), //回放方式
        'playBackRatio': Number(selectPlaySpeed.options[selectPlaySpeedIndex].value), //回放方式速度
        'startTime': startTime, //开始时间
        'endTime': endTime //结束时间
    }
    const reqObj = {
        'action': 'StartHist',
        'uuid': uuid,
        'params': paramsJson,
        'audio': audio,
        'video': video
    }
    sendMessage(reqObj);
}

// 直播控制请求
function liveControlRequest() {
    const controlLiveChannel = document.getElementById('controlLiveChannel');
    const selectControlLiveChannelIndex = controlLiveChannel.selectedIndex;
    if (selectControlLiveChannelIndex < 1) {
        alert('未选择直播通道');
        return;
    }
    const commandTypeSelect = document.getElementById('commandType');
    const selectCommandTypeIndex = commandTypeSelect.selectedIndex;
    const closeTypeSelect = document.getElementById('closeType');
    const selectCloseTypeIndex = closeTypeSelect.selectedIndex;
    const switchTypeSelect = document.getElementById('switchType');
    const selectSwitchTypeIndex = switchTypeSelect.selectedIndex;
    command = Number(commandTypeSelect.options[selectCommandTypeIndex].value);
    const channelId = Number(controlLiveChannel.options[selectControlLiveChannelIndex].value);
    const reqObj = {
        'action': 'ControlLive',
        'uuid': uuid,
        'params': {
            'deviceId': deviceId, //设备编号
            'channel': channelId, //通道号
            'command': command, //关闭指令
            'closeType': Number(closeTypeSelect.options[selectCloseTypeIndex].value), //关闭类型
            'switchType': Number(switchTypeSelect.options[selectSwitchTypeIndex].value), // 切换码流
        }
    }
    sendMessage(reqObj);
    if (command === 0) {
        stopPlayFlag[channelId] = true;
        console.info('stopPlayFlag ===> ', stopPlayFlag);
    }
}

function playVideo(videoElementId, playUrl, isLive, channelId) {
    let videoElement = document.getElementById(videoElementId);
    if (!videoElement) {
        createPlayVideoBlock(videoElementId);
        videoElement = document.getElementById(videoElementId);
    }
    stopPlayFlag[channelId] = false;
    if (flvjs.isSupported()) {
        let flvPlayer = flvjs.createPlayer({
            type: 'flv',
            url: playUrl,
            isLive: isLive,
            enableStashBuffer: false
        }, {
            enableWorker: false, //不启用分离线程
            enableStashBuffer: false, //关闭IO隐藏缓冲区
            autoCleanupSourceBuffer: true //自动清除缓存
        });
        flvPlayer.attachMediaElement(videoElement);
        flvPlayer.load();
        flvPlayer.play();

        flvPlayer.on(flvjs.Events.STATISTICS_INFO, (res) => {
            if (stopPlayFlag[channelId] === true) {
                // 离开路由或切换设备
                console.log("销毁flvPlayer实例")
                // 销毁实例
                flvPlayer.pause();
                flvPlayer.unload();
                flvPlayer.detachMediaElement();
                flvPlayer.destroy();
                flvPlayer = null;
                document.getElementById('liveChannelList').removeChild(videoElement.parentNode);
            }
        })
    } else {
        alert('浏览器不支持flv视频播放');
        console.error('浏览器不支持flv视频播放');
    }
}

// 播放请求响应处理
function playAction(data) {
    const status = data.status;
    if (status !== 0) {
        alert('发送直播请求响应出错');
        console.error('发送直播请求响应出错，', JSON.stringify(data));
        return;
    }
    const channelNum = data.channelNum;
    var rtmpUrl = data.rtmpUrl; // rtmp流，提供给app端播放
    let url = data.url; // flv格式，提供给web端播放
    if (data.httpsUrl) { // 如果https可用，就用https
        url = data.httpsUrl;
    }
    const channelIdStr = getChannelId(channelNum);
    const isLive = (data.isLive === 0);
    /*if (!isLive) {
        url = data.hlsUrl;
    }*/
    playVideo(channelIdStr, url, isLive, Number(channelNum));
}

// 直播请求
function liveRequest() {
    const liveChannel = document.getElementById('liveChannel');
    const selectChannelIndex = liveChannel.selectedIndex;
    if (selectChannelIndex < 1) {
        alert('未选择直播通道');
        return;
    }
    const mediaTypeSelect = document.getElementById('MediaType');
    const selectMediaTypeIndex = mediaTypeSelect.selectedIndex;
    const streamTypeSelect = document.getElementById('StreamType');
    const selectStreamTypeIndex = streamTypeSelect.selectedIndex;

    const reqObj = {
        "action": "StartLive",
        "uuid": uuid,
        "params": {
            "deviceId": deviceId,
            "channel": Number(liveChannel.options[selectChannelIndex].value),
            "type": Number(mediaTypeSelect.options[selectMediaTypeIndex].value),
            "streamType": Number(streamTypeSelect.options[selectStreamTypeIndex].value)
        }
    };
    sendMessage(reqObj);
}

// 查询设备属性请求响应处理
function queryAttributeAction(data) {
    const maxVideoChannelNum = data.maxVideoChannelNum;
    const maxAudioChannelNum = data.maxAudioChannelNum;
    if (maxAudioChannelNum < 1 || maxVideoChannelNum < 1) {
        alert('未查询到设备支持的音视频通道');
        return;
    }
    const supportAudioOutput = data.supportAudioOutput;
    console.log('设备支持的最大视频通道：', maxVideoChannelNum, '最大音频通道：', maxAudioChannelNum, '是否支持音频输出：', supportAudioOutput);
    const liveChannel = document.getElementById('liveChannel');
    const controlLiveChannel = document.getElementById('controlLiveChannel');
    for (let i = 1; i <= maxVideoChannelNum; i++) {
        liveChannel.options.add(new Option('通道 ' + i + '', i));
        controlLiveChannel.options.add(new Option('通道 ' + i + '', i));
        if (i === 1) {
            liveChannel.options[1].selected = true;
            controlLiveChannel.options[1].selected = true;
        }
    }
    alert('查询设备属性请求处理成功');
}

// 查询设备属性
function queryAttributeRequest() {
    checkConnect();
    deviceId = document.getElementById('deviceId').value;
    if(!deviceId){
        alert('设备号不能为空');
    }
    const reqObj = {
        'action': 'QueryAttribute',
        'uuid': uuid,
        'deviceId': deviceId
    };
    sendMessage(reqObj);
}

// WebSocket链接检测
function checkConnect() {
    if (!ws) {
        alert('WebSocket服务未连接');
        console.error('WebSocket server disconnect');
    }
}

// 请求响应分发处理
function doAction(dataStr) {
    const data = JSON.parse(dataStr);
    const action = data.action;
    switch (action) {
        case 'Hello':
            console.log('WebSocket客户端注册成功');
            break;
        case 'QueryAttribute':
            queryAttributeAction(data);
            break;
        case 'Play':
            playAction(data);
            break;
        case 'Response':
            responseAction(data);
            break;
        default:
            console.error('未找到处理方法,', data);
            break;
    }
}

// 注册WebSocket
function register() {
    uuid = createUUID();
    const obj = {
        'action': 'Hello',
        'terminal': 'Brower',
        'uuid': uuid
    };
    sendMessage(obj);
}

function openConnect() {
    alert('WebSocket连接成功');
    console.log('WebSocket连接成功');
    register();
}

// 发送WebSocket消息
function sendMessage(obj) {
    const msg = JSON.stringify(obj);
    console.log('send message : ', msg);
    checkConnect();
    ws.send(msg);
}

// 接收到WebSocket返回的消息
function receivedMessage(event) {
    const data = event.data;
    console.log('received server message : ', data);
    doAction(data);
}

function closeConnect(e) {
    ws = null;
    console.log('WebSocket server disconnect', e);
}

function errorConnect(e) {
    ws = null;
    console.error('WebSocket server error , ', e);
}

function connectServerRequest() {
    const url = document.getElementById('socketUrl').value;
    console.log('WebSocket url === ', url)
    if (!url) {
        console.error('WebSocket链接地址不能为空');
        return;
    }
    ws = new WebSocket(url);
    ws.onopen = openConnect;
    ws.onmessage = receivedMessage;
    ws.onclose = closeConnect;
    ws.onerror = errorConnect;
}

// 页面加载后的初始化方法
function init() {
    const connectServerBtn = document.getElementById('connectServerBtn');
    connectServerBtn.onclick = connectServerRequest;
    const queryAttributeBtn = document.getElementById('queryAttributeBtn');
    queryAttributeBtn.onclick = queryAttributeRequest;
    const startLiveBtn = document.getElementById('StartLiveBtn');
    startLiveBtn.onclick = liveRequest;
    const liveControlBtn = document.getElementById('liveControlBtn');
    liveControlBtn.onclick = liveControlRequest;
    const playHistoryBtn = document.getElementById('playHistoryBtn');
    playHistoryBtn.onclick = playHistoryRequest;
    const controllerHistoryBtn = document.getElementById('controllerHistoryBtn');
    controllerHistoryBtn.onclick = historyControlRequest;
    const std = new Date(new Date().setMinutes(new Date().getMinutes() - 30));
    const etd = new Date();
    document.getElementById('startTime_his').value = getFormatDateTime(std);
    document.getElementById('endTime_his').value = getFormatDateTime(etd);
    document.getElementById('dragPlayPosition_his').value = getFormatDateTime(etd);
}

window.onload = init;
