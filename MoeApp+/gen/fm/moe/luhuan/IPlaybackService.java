/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: D:\\git\\MoeFM\\MoeApp+\\src\\fm\\moe\\luhuan\\IPlaybackService.aidl
 */
package fm.moe.luhuan;
public interface IPlaybackService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements fm.moe.luhuan.IPlaybackService
{
private static final java.lang.String DESCRIPTOR = "fm.moe.luhuan.IPlaybackService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an fm.moe.luhuan.IPlaybackService interface,
 * generating a proxy if needed.
 */
public static fm.moe.luhuan.IPlaybackService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof fm.moe.luhuan.IPlaybackService))) {
return ((fm.moe.luhuan.IPlaybackService)iin);
}
return new fm.moe.luhuan.IPlaybackService.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_getNowIndex:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getNowIndex();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSongDuration:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSongDuration();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_getSongCurrentPosition:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getSongCurrentPosition();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_playSongAtIndex:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.playSongAtIndex(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_playNext:
{
data.enforceInterface(DESCRIPTOR);
this.playNext();
reply.writeNoException();
return true;
}
case TRANSACTION_playPrevious:
{
data.enforceInterface(DESCRIPTOR);
this.playPrevious();
reply.writeNoException();
return true;
}
case TRANSACTION_pause:
{
data.enforceInterface(DESCRIPTOR);
this.pause();
reply.writeNoException();
return true;
}
case TRANSACTION_start:
{
data.enforceInterface(DESCRIPTOR);
this.start();
reply.writeNoException();
return true;
}
case TRANSACTION_isPlayerPrepared:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlayerPrepared();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_isPlayerPlaying:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.isPlayerPlaying();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_setAsForeGround:
{
data.enforceInterface(DESCRIPTOR);
this.setAsForeGround();
reply.writeNoException();
return true;
}
case TRANSACTION_seekTo:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.seekTo(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_stopAsForeGround:
{
data.enforceInterface(DESCRIPTOR);
this.stopAsForeGround();
reply.writeNoException();
return true;
}
case TRANSACTION_getList:
{
data.enforceInterface(DESCRIPTOR);
java.util.List<fm.moe.luhuan.beans.data.SimpleData> _result = this.getList();
reply.writeNoException();
reply.writeTypedList(_result);
return true;
}
case TRANSACTION_getCurrentItem:
{
data.enforceInterface(DESCRIPTOR);
fm.moe.luhuan.beans.data.SimpleData _result = this.getCurrentItem();
reply.writeNoException();
if ((_result!=null)) {
reply.writeInt(1);
_result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_getListSize:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.getListSize();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
case TRANSACTION_addItem:
{
data.enforceInterface(DESCRIPTOR);
fm.moe.luhuan.beans.data.SimpleData _arg0;
if ((0!=data.readInt())) {
_arg0 = fm.moe.luhuan.beans.data.SimpleData.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.addItem(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeItem:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.removeItem(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_clearList:
{
data.enforceInterface(DESCRIPTOR);
this.clearList();
reply.writeNoException();
return true;
}
case TRANSACTION_randPlay:
{
data.enforceInterface(DESCRIPTOR);
this.randPlay();
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements fm.moe.luhuan.IPlaybackService
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public int getNowIndex() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getNowIndex, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSongDuration() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSongDuration, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getSongCurrentPosition() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getSongCurrentPosition, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void playSongAtIndex(int n) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(n);
mRemote.transact(Stub.TRANSACTION_playSongAtIndex, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playNext() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_playNext, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void playPrevious() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_playPrevious, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void pause() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_pause, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void start() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_start, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public boolean isPlayerPrepared() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlayerPrepared, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean isPlayerPlaying() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_isPlayerPlaying, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void setAsForeGround() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_setAsForeGround, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void seekTo(int n) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(n);
mRemote.transact(Stub.TRANSACTION_seekTo, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void stopAsForeGround() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_stopAsForeGround, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public java.util.List<fm.moe.luhuan.beans.data.SimpleData> getList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.util.List<fm.moe.luhuan.beans.data.SimpleData> _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getList, _data, _reply, 0);
_reply.readException();
_result = _reply.createTypedArrayList(fm.moe.luhuan.beans.data.SimpleData.CREATOR);
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public fm.moe.luhuan.beans.data.SimpleData getCurrentItem() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
fm.moe.luhuan.beans.data.SimpleData _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getCurrentItem, _data, _reply, 0);
_reply.readException();
if ((0!=_reply.readInt())) {
_result = fm.moe.luhuan.beans.data.SimpleData.CREATOR.createFromParcel(_reply);
}
else {
_result = null;
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int getListSize() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getListSize, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public void addItem(fm.moe.luhuan.beans.data.SimpleData item) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((item!=null)) {
_data.writeInt(1);
item.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_addItem, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void removeItem(int index) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(index);
mRemote.transact(Stub.TRANSACTION_removeItem, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void clearList() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_clearList, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
@Override public void randPlay() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_randPlay, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_getNowIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getSongDuration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getSongCurrentPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_playSongAtIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_playNext = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_playPrevious = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_isPlayerPrepared = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
static final int TRANSACTION_isPlayerPlaying = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
static final int TRANSACTION_setAsForeGround = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
static final int TRANSACTION_seekTo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
static final int TRANSACTION_stopAsForeGround = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
static final int TRANSACTION_getList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
static final int TRANSACTION_getCurrentItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
static final int TRANSACTION_getListSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
static final int TRANSACTION_addItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
static final int TRANSACTION_removeItem = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
static final int TRANSACTION_clearList = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
static final int TRANSACTION_randPlay = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
}
public int getNowIndex() throws android.os.RemoteException;
public int getSongDuration() throws android.os.RemoteException;
public int getSongCurrentPosition() throws android.os.RemoteException;
public void playSongAtIndex(int n) throws android.os.RemoteException;
public void playNext() throws android.os.RemoteException;
public void playPrevious() throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void start() throws android.os.RemoteException;
public boolean isPlayerPrepared() throws android.os.RemoteException;
public boolean isPlayerPlaying() throws android.os.RemoteException;
public void setAsForeGround() throws android.os.RemoteException;
public void seekTo(int n) throws android.os.RemoteException;
public void stopAsForeGround() throws android.os.RemoteException;
public java.util.List<fm.moe.luhuan.beans.data.SimpleData> getList() throws android.os.RemoteException;
public fm.moe.luhuan.beans.data.SimpleData getCurrentItem() throws android.os.RemoteException;
public int getListSize() throws android.os.RemoteException;
public void addItem(fm.moe.luhuan.beans.data.SimpleData item) throws android.os.RemoteException;
public void removeItem(int index) throws android.os.RemoteException;
public void clearList() throws android.os.RemoteException;
public void randPlay() throws android.os.RemoteException;
}
