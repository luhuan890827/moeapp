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
}
static final int TRANSACTION_getNowIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_getSongDuration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_getSongCurrentPosition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_playSongAtIndex = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_pause = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_start = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_isPlayerPrepared = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_isPlayerPlaying = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
}
public int getNowIndex() throws android.os.RemoteException;
public int getSongDuration() throws android.os.RemoteException;
public int getSongCurrentPosition() throws android.os.RemoteException;
public void playSongAtIndex(int n) throws android.os.RemoteException;
public void pause() throws android.os.RemoteException;
public void start() throws android.os.RemoteException;
public boolean isPlayerPrepared() throws android.os.RemoteException;
public boolean isPlayerPlaying() throws android.os.RemoteException;
}
