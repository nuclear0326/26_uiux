import wave,json
from pathlib import Path
import numpy as np
root=Path(__file__).resolve().parents[1]
report=json.loads((root/'audio_validation.json').read_text())
assert len(report)==20
for name,r in report.items():
 assert 0<r['rms']<.2 and r['peak']<.9,name
 with wave.open(str(root/'src/assets/audio'/f'{name}.wav')) as w:x=np.frombuffer(w.readframes(w.getnframes()),dtype='<i2')
 if name.startswith(('bgm/','ambience/')):
  # Broadband rain has naturally large adjacent-sample differences; compare
  # the wrap to the signal's own differences instead of a low-frequency limit.
  y=x.astype(np.float64)/32768
  limit=max(.001,float(np.quantile(np.abs(np.diff(y)),.999))*1.2)
  assert abs(y[-1]-y[0])<=limit,(name,r,limit)
 else:
  assert abs(int(x[0]))<=2 and abs(int(x[-1]))<=2,name
with wave.open(str(root/'src/assets/audio/ambience/clockwork.wav')) as w:x=np.frombuffer(w.readframes(w.getnframes()),dtype='<i2')/32768;sr=w.getframerate()
for i in range(64):
 start=int((.25+i*.5)*sr)
 on=np.mean(x[start:start+int(.06*sr)]**2)
 off=np.mean(x[start-int(.10*sr):start-int(.02*sr)]**2)
 assert on>100*max(off,1e-12),i
print('Peak, loop boundary, effect endpoint and 64 half-second clock beat checks passed.')
