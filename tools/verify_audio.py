"""Validate currently shipped audio files without opening a playback device."""
import wave
from pathlib import Path
root=Path(__file__).resolve().parents[1]/'src/assets/audio'
count=0
for path in sorted(root.rglob('*.wav')):
 with wave.open(str(path)) as audio:
  assert audio.getsampwidth()==2 and audio.getnchannels() in (1,2),path
  assert audio.getframerate()>0 and audio.getnframes()>0,path
  assert len(audio.readframes(audio.getnframes()))==audio.getnframes()*audio.getnchannels()*2,path
 count+=1
print('Validated',count,'current PCM audio resources; no playback.')
