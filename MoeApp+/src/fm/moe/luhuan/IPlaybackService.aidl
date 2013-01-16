package fm.moe.luhuan;
import fm.moe.luhuan.beans.data.SimpleData;
interface IPlaybackService{
int getNowIndex();
int getSongDuration();
int getSongCurrentPosition();
void playSongAtIndex(int n);
void playNext();
void playPrevious();
void pause();
void start();
boolean isPlayerPrepared();
boolean isPlayerPlaying();
void setAsForeGround();
void seekTo(int n);
void stopAsForeGround();
List<SimpleData> getList();
SimpleData getCurrentItem();
int getListSize();
void addItem(in SimpleData item);
void removeItem(int index);
void clearList();
void randPlay();
}