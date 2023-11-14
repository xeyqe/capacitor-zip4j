import type { PluginListenerHandle } from '@capacitor/core';
import type { Plugin } from '@capacitor/core/types/definitions';

export interface Zip4JPlugin extends Plugin {
  extractAll(options: { zipFilePath: string, destDirectory: string, password?: string }): Promise<{ message: string }>;
  extractFile(options: { zipFilePath: string, destDirectory: string, fileName: string, newFileName: string, password?: string }): Promise<{ message: string }>;
  removeFiles(options: { zipFilePath: string, fileNames: string[], password?: string }): Promise<{ message: string }>;
  renameFile(options: { zipFilePath: string, oldFile: string, newFile: string, password?: string }): Promise<{ message: string }>;
  mergeSplitFiles(options: { zipFilePath: string, newZipFilePath: string, password?: string }): Promise<{ message: string }>;
  getAllFileNames(options: { zipFilePath: string, password?: string }): Promise<{ names: string[] }>;
  isEncrypted(options: { zipFilePath: string }): Promise<{ isEncrypted: boolean }>;
  isSplitArchive(options: { zipFilePath: string, password?: string }): Promise<{ isSplitArchive: boolean }>;
  setComment(options: { zipFilePath: string, comment: string, password?: string }): Promise<{ message: string }>;
  getComment(options: { zipFilePath: string, password?: string }): Promise<{ comment: string }>;
  containsFile(options: { zipFilePath: string, fileName: string, password?: string }): Promise<{ contains: boolean }>;
  addFiles(options: { zipFilePath: string, files: string[], password?: string }): Promise<{ message: string }>;
  addFolder(options: { zipFilePath: string, folder: string, password?: string }): Promise<{ message: string }>;
  createSplitZipFile(options: { zipFilePath: string, files: string[], splitLength: number, password?: string }): Promise<{ message: string }>;
  addListener(
    eventName: 'extractAllProgressEvent' | 'renameFileProgressEvent' | 'removeFilesProgressEvent' | 'renameFileProgressEvent' | 'mergeSplitFilesProgressEvent' | 'setCommentProgressEvent' | 'addFilesProgressEvent' | 'addFolderProgressEvent' | 'createSplitZipFileProgressEvent',
    listenerFunc: (obj: { progress: number }) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
