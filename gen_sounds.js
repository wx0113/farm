#!/usr/bin/env node
/**
 * 生成农场游戏 WAV 音效文件
 * Usage: node gen_sounds.js
 */
const fs = require('fs');
const path = require('path');

const SAMPLE_RATE = 44100;
const OUT_DIR = path.join(__dirname, 'src', 'main', 'resources', 'static', 'seed', 'sounds');

// ========== 波形生成器 ==========

function genSine(freq, duration, vol = 1.0) {
    const n = Math.floor(SAMPLE_RATE * duration);
    const samples = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        samples[i] = Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) * vol;
    }
    return samples;
}

function genTriangle(freq, duration, vol = 1.0) {
    const n = Math.floor(SAMPLE_RATE * duration);
    const samples = new Float32Array(n);
    const period = SAMPLE_RATE / freq;
    for (let i = 0; i < n; i++) {
        samples[i] = (2 * Math.abs(2 * (i / period - Math.floor(i / period + 0.5))) - 1) * vol;
    }
    return samples;
}

function genSquare(freq, duration, vol = 1.0) {
    const n = Math.floor(SAMPLE_RATE * duration);
    const samples = new Float32Array(n);
    const period = SAMPLE_RATE / freq;
    for (let i = 0; i < n; i++) {
        samples[i] = ((i % period < period / 2) ? 1 : -1) * vol;
    }
    return samples;
}

function genSawtooth(freq, duration, vol = 1.0) {
    const n = Math.floor(SAMPLE_RATE * duration);
    const samples = new Float32Array(n);
    const period = SAMPLE_RATE / freq;
    for (let i = 0; i < n; i++) {
        samples[i] = (2 * (i / period - Math.floor(i / period)) - 1) * vol;
    }
    return samples;
}

function genNoise(duration, vol = 1.0) {
    const n = Math.floor(SAMPLE_RATE * duration);
    const samples = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        samples[i] = (Math.random() * 2 - 1) * vol;
    }
    return samples;
}

// ========== 包络 (ADSR) ==========

function applyEnvelope(samples, attack = 0.01, decay = 0.05, sustain = 0.7, release = 0.15) {
    const n = samples.length;
    const aLen = Math.floor(attack * SAMPLE_RATE);
    const dLen = Math.floor(decay * SAMPLE_RATE);
    const rLen = Math.floor(release * SAMPLE_RATE);
    const sLen = Math.max(0, n - aLen - dLen - rLen);

    const result = new Float32Array(n);
    for (let i = 0; i < aLen; i++) result[i] = samples[i] * (i / aLen);
    for (let i = 0; i < dLen; i++) result[aLen + i] = samples[aLen + i] * (1 - (1 - sustain) * i / dLen);
    for (let i = 0; i < sLen; i++) result[aLen + dLen + i] = samples[aLen + dLen + i] * sustain;
    for (let i = 0; i < rLen; i++) {
        const idx = aLen + dLen + sLen + i;
        if (idx < n) result[idx] = samples[idx] * sustain * (1 - i / rLen);
    }
    return result;
}

// ========== 混合 ==========

function mix(...tracks) {
    const length = Math.max(...tracks.map(t => t.length));
    const result = new Float32Array(length);
    for (const t of tracks) {
        for (let i = 0; i < t.length; i++) {
            result[i] += t[i];
        }
    }
    // 归一化
    let maxVal = 0;
    for (const v of result) maxVal = Math.max(maxVal, Math.abs(v));
    if (maxVal > 0) {
        const scale = 0.9 / maxVal;
        for (let i = 0; i < result.length; i++) result[i] *= scale;
    }
    return result;
}

function note(freq, duration, waveform = 'sine', vol = 1.0) {
    const wfMap = { sine: genSine, triangle: genTriangle, square: genSquare, sawtooth: genSawtooth };
    const gen = wfMap[waveform] || genSine;
    const samples = gen(freq, duration, vol);
    return applyEnvelope(samples, 0.005, 0.03, 0.8, 0.05);
}

function melody(notes, waveform = 'sine', vol = 0.8) {
    let parts = [];
    for (const [freq, dur] of notes) {
        parts.push(note(freq, dur, waveform, vol));
    }
    // 拼接
    const totalLen = parts.reduce((sum, p) => sum + p.length, 0);
    const result = new Float32Array(totalLen);
    let offset = 0;
    for (const p of parts) {
        result.set(p, offset);
        offset += p.length;
    }
    return result;
}

// ========== WAV 保存 ==========

function saveWav(filepath, samples) {
    const buf = Buffer.alloc(44 + samples.length * 2);

    // RIFF header
    buf.write('RIFF', 0);
    buf.writeUInt32LE(36 + samples.length * 2, 4);
    buf.write('WAVE', 8);

    // fmt chunk
    buf.write('fmt ', 12);
    buf.writeUInt32LE(16, 16);        // chunk size
    buf.writeUInt16LE(1, 20);         // PCM format
    buf.writeUInt16LE(1, 22);         // mono
    buf.writeUInt32LE(SAMPLE_RATE, 24);
    buf.writeUInt32LE(SAMPLE_RATE * 2, 28); // byte rate
    buf.writeUInt16LE(2, 32);         // block align
    buf.writeUInt16LE(16, 34);        // bits per sample

    // data chunk
    buf.write('data', 36);
    buf.writeUInt32LE(samples.length * 2, 40);

    // PCM samples
    for (let i = 0; i < samples.length; i++) {
        const s = Math.max(-1, Math.min(1, samples[i]));
        buf.writeInt16LE(Math.round(s * 32767), 44 + i * 2);
    }

    fs.writeFileSync(filepath, buf);
}

// ======================== 音效定义 ========================

function makePlant() {
    const s = melody([[523, 0.10], [659, 0.10], [784, 0.18]], 'triangle', 0.7);
    return applyEnvelope(s, 0.005, 0.02, 0.8, 0.08);
}

function makeHarvest() {
    const main = melody([[523, 0.12], [659, 0.12], [784, 0.12], [1047, 0.35]], 'triangle', 0.75);
    const high = melody([[1047, 0.12], [1318, 0.12], [1568, 0.12], [2093, 0.35]], 'sine', 0.3);
    return mix(main, high);
}

function makeKillworm() {
    const noisePart = applyEnvelope(genNoise(0.08, 0.6), 0.001, 0.03, 0.0, 0.05);
    const tonePart = applyEnvelope(melody([[900, 0.06], [1200, 0.1]], 'square', 0.5), 0.001, 0.02, 0.6, 0.04);
    return mix(noisePart, tonePart);
}

function makeCleanland() {
    const dur = 0.45;
    const n = Math.floor(SAMPLE_RATE * dur);
    const sweep = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        const freq = 300 + 800 * (i / n);
        sweep[i] = Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) * 0.5;
    }
    const sweepEnv = applyEnvelope(sweep, 0.01, 0.05, 0.6, 0.1);
    const tail = applyEnvelope(melody([[1000, 0.15]], 'triangle', 0.4), 0.001, 0.05, 0.5, 0.1);
    // 拼接：sweep + gap + tail
    const gap = Math.floor(SAMPLE_RATE * 0.02);
    const result = new Float32Array(sweepEnv.length + gap + tail.length);
    result.set(sweepEnv, 0);
    result.set(tail, sweepEnv.length + gap);
    return result;
}

function makeWormalert() {
    const alertTone = melody([[440, 0.15], [350, 0.15], [440, 0.15], [350, 0.15], [440, 0.25]], 'sawtooth', 0.5);
    const noisePart = applyEnvelope(genNoise(0.85, 0.25), 0.01, 0.1, 0.4, 0.1);
    // 对齐长度
    const maxLen = Math.max(alertTone.length, noisePart.length);
    const paddedTone = new Float32Array(maxLen);
    paddedTone.set(alertTone, 0);
    const paddedNoise = new Float32Array(maxLen);
    paddedNoise.set(noisePart, 0);
    return mix(paddedTone, paddedNoise);
}

function makeMature() {
    const main = melody([[784, 0.15], [988, 0.15], [1175, 0.15], [1318, 0.5]], 'sine', 0.75);
    const high = melody([[1568, 0.15], [1976, 0.15], [2349, 0.15], [2637, 0.5]], 'sine', 0.25);
    return mix(main, high);
}

function makeStagechange() {
    const s = note(880, 0.06, 'sine', 0.5);
    return applyEnvelope(s, 0.001, 0.04, 0.1, 0.02);
}

function makeClick() {
    // 短促清脆的点击声 — 高频正弦 + 噪声混合
    const tick = note(2400, 0.015, 'sine', 0.7);
    const click = applyEnvelope(tick, 0.001, 0.01, 0.1, 0.005);
    const noiseBurst = applyEnvelope(genNoise(0.02, 0.35), 0.001, 0.008, 0.05, 0.005);
    return mix(click, noiseBurst);
}

function makeTaunt() {
    // 下滑主音
    const dur = 0.35;
    const n = Math.floor(SAMPLE_RATE * dur);
    const slide = new Float32Array(n);
    for (let i = 0; i < n; i++) {
        const freq = 600 - 400 * (i / n);
        slide[i] = Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) * 0.4;
    }
    const slideEnv = applyEnvelope(slide, 0.005, 0.1, 0.5, 0.08);

    // 嘟声
    const beep = melody([[250, 0.3]], 'square', 0.3);

    // 低沉尾音
    const low = melody([[150, 0.3]], 'triangle', 0.3);

    // 延迟偏移
    const pad1 = Math.floor(SAMPLE_RATE * 0.2);
    const pad2 = Math.floor(SAMPLE_RATE * 0.4);
    const totalLen = slideEnv.length + pad1 + beep.length + pad2 + low.length;
    const full = new Float32Array(totalLen);
    full.set(slideEnv, 0);
    full.set(beep, slideEnv.length + pad1);
    full.set(low, slideEnv.length + pad1 + beep.length + pad2);

    return full;
}

// ======================== 主程序 ========================

const SOUNDS = {
    'plant.wav':       ['播种成功', makePlant],
    'harvest.wav':     ['收获成功', makeHarvest],
    'killworm.wav':    ['除虫成功', makeKillworm],
    'cleanland.wav':   ['除枯草',   makeCleanland],
    'wormalert.wav':   ['虫害警报', makeWormalert],
    'mature.wav':      ['作物成熟', makeMature],
    'stagechange.wav': ['阶段切换', makeStagechange],
    'taunt.wav':       ['嘲讽失败', makeTaunt],
    'click.wav':       ['点击土地', makeClick],
};

function main() {
    fs.mkdirSync(OUT_DIR, { recursive: true });

    for (const [filename, [desc, factory]] of Object.entries(SOUNDS)) {
        const filepath = path.join(OUT_DIR, filename);
        const samples = factory();
        saveWav(filepath, samples);
        const duration = samples.length / SAMPLE_RATE;
        console.log(`✅ ${filename} (${desc}) — ${duration.toFixed(2)}s — ${samples.length} samples`);
    }
    console.log(`\n共生成 ${Object.keys(SOUNDS).length} 个音效文件 → ${OUT_DIR}`);
}

main();
