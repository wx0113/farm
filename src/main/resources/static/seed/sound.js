/**
 * 游戏音效管理器 - 使用 Web Audio API 合成音效
 */
var Sound = (function() {
    var ctx = null;
    function getCtx() {
        if (!ctx) {
            ctx = new (window.AudioContext || window.webkitAudioContext)();
        }
        return ctx;
    }

    /** 播放一个频率的音调 */
    function tone(freq, duration, type, vol, rampDown) {
        try {
            var c = getCtx();
            var osc = c.createOscillator();
            var gain = c.createGain();
            osc.type = type || 'sine';
            osc.frequency.value = freq;
            gain.gain.setValueAtTime(vol || 0.3, c.currentTime);
            if (rampDown) {
                gain.gain.exponentialRampToValueAtTime(0.001, c.currentTime + duration);
            }
            osc.connect(gain);
            gain.connect(c.destination);
            osc.start(c.currentTime);
            osc.stop(c.currentTime + duration);
        } catch(e) {}
    }

    /** 播放旋律（音符数组 [{freq, dur}]） */
    function melody(notes, type, vol) {
        try {
            var c = getCtx();
            var t = c.currentTime;
            notes.forEach(function(n) {
                var osc = c.createOscillator();
                var gain = c.createGain();
                osc.type = type || 'sine';
                osc.frequency.value = n[0];
                gain.gain.setValueAtTime((vol || 0.25), t);
                gain.gain.exponentialRampToValueAtTime(0.001, t + n[1]);
                osc.connect(gain);
                gain.connect(c.destination);
                osc.start(t);
                osc.stop(t + n[1]);
                t += n[1];
            });
        } catch(e) {}
    }

    return {
        /** 播种成功 */
        plant: function() {
            melody([[523,0.1],[659,0.1],[784,0.2]], 'triangle', 0.3);
        },
        /** 播种失败 / 负面消息 */
        fail: function() {
            melody([[400,0.2],[300,0.2],[200,0.4]], 'sawtooth', 0.2);
        },
        /** 除虫成功 */
        killWorm: function() {
            melody([[800,0.08],[1000,0.08],[1200,0.15]], 'square', 0.2);
        },
        /** 收获成功 */
        harvest: function() {
            melody([[523,0.12],[659,0.12],[784,0.12],[1047,0.3]], 'triangle', 0.35);
        },
        /** 除枯草 */
        cleanLand: function() {
            melody([[600,0.1],[800,0.1],[600,0.1],[1000,0.2]], 'triangle', 0.25);
        },
        /** 生虫警报 */
        wormAlert: function() {
            melody([[440,0.15],[350,0.15],[440,0.15],[350,0.15],[440,0.3]], 'sawtooth', 0.2);
        },
        /** 作物成熟 */
        mature: function() {
            melody([[784,0.15],[988,0.15],[1175,0.15],[1318,0.4]], 'sine', 0.3);
        },
        /** 阶段切换 */
        stageChange: function() {
            tone(880, 0.05, 'sine', 0.15, true);
        }
    };
})();
