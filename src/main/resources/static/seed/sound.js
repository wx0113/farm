/**
 * 游戏音效管理器 — HTML5 Audio
 * 使用生成的 WAV 音效文件，兼容性更好
 */
var Sound = (function() {
    // 预加载所有音效
    var sounds = {};
    var basePath = 'sounds/';
    var ready = false;
    var preloadCount = 0;
    var totalCount = 0;

    var soundList = {
        plant:       'plant.wav',
        harvest:     'harvest.wav',
        killWorm:    'killworm.wav',
        cleanLand:   'cleanland.wav',
        wormAlert:   'wormalert.wav',
        mature:      'mature.wav',
        stageChange: 'stagechange.wav',
        taunt:       'taunt.wav',
        fail:        'taunt.wav',   // fail 复用 taunt
        click:       'click.wav'
    };

    function init() {
        if (ready) return;
        var keys = Object.keys(soundList);
        totalCount = keys.length;

        keys.forEach(function(key) {
            var audio = new Audio();
            audio.src = basePath + soundList[key];
            audio.preload = 'auto';
            audio.volume = key === 'wormAlert' ? 0.7 : 0.5;
            audio.addEventListener('canplaythrough', function() {
                preloadCount++;
            }, { once: true });
            audio.addEventListener('error', function() {
                console.warn('音效加载失败: ' + soundList[key]);
                preloadCount++;
            }, { once: true });
            sounds[key] = audio;
        });
        ready = true;
    }

    function play(name) {
        if (!ready) init();
        var audio = sounds[name];
        if (audio) {
            try {
                audio.currentTime = 0;
                var p = audio.play();
                if (p && p.catch) {
                    p.catch(function(e) {
                        // 浏览器可能在无交互时禁止播放，忽略
                    });
                }
            } catch(e) {
                // 静默失败
            }
        }
    }

    // 自动初始化
    if (document.readyState === 'interactive' || document.readyState === 'complete') {
        setTimeout(init, 100);
    } else {
        document.addEventListener('DOMContentLoaded', function() { setTimeout(init, 100); });
    }

    return {
        plant:       function() { play('plant'); },
        harvest:     function() { play('harvest'); },
        killWorm:    function() { play('killWorm'); },
        cleanLand:   function() { play('cleanLand'); },
        wormAlert:   function() { play('wormAlert'); },
        mature:      function() { play('mature'); },
        stageChange: function() { play('stageChange'); },
        taunt:       function() { play('taunt'); },
        fail:        function() { play('fail'); },
        click:       function() { play('click'); },
        init:        init,
        isReady:     function() { return preloadCount >= totalCount; }
    };
})();
