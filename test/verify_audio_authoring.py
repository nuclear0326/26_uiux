"""Authoring safety checks; never play audio or launch the game."""
from pathlib import Path
import hashlib,json,subprocess,sys,tempfile,wave
repo=Path(__file__).resolve().parents[1];script=repo/'tools/authoring/generate_audio.py'
def snapshot():return {str(p.relative_to(repo)):hashlib.sha256(p.read_bytes()).hexdigest() for p in (repo/'src/assets/audio').rglob('*') if p.is_file()}
before=snapshot();target=Path(tempfile.mkdtemp(prefix='audio-authoring-check-'));runs=target/'outputs/audio-authoring';runs.mkdir(parents=True)
sentinel=runs/'existing.txt';sentinel.write_text('preserve existing drafts')
for _ in range(2):subprocess.run([sys.executable,str(script),'--artifacts-root',str(target)],cwd=target,check=True,capture_output=True)
drafts=[p for p in runs.iterdir() if p.is_dir()];assert len(drafts)==2
for folder in drafts:
 report=json.loads((folder/'audio_validation.json').read_text());assert len(report)==18
 for name in report:
  with wave.open(str(folder/(name+'.wav'))) as audio:assert audio.getnchannels()==1 and audio.getsampwidth()==2 and audio.getnframes()>0
assert sentinel.read_text()=='preserve existing drafts'
result=subprocess.run([sys.executable,str(script),'--artifacts-root',str(repo)],capture_output=True)
assert result.returncode!=0 and b'outside the repository' in result.stderr
assert before==snapshot();assert not (repo/'audio_validation.json').exists()
print('PASS: two distinct drafts, 36 valid WAVs, existing drafts preserved, repository output blocked, game audio unchanged; no playback')
