import { PluginListenerHandle, WebPlugin } from '@capacitor/core';

import type { Zip4JPlugin } from './definitions';

export class Zip4JWeb extends WebPlugin implements Zip4JPlugin {
  extractAll(options: { zipFilePath: string; destDirectory: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  extractFile(options: { zipFilePath: string; destDirectory: string; fileName: string; newFileName: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  removeFiles(options: { zipFilePath: string; fileNames: string[]; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  renameFile(options: { zipFilePath: string; oldFile: string; newFile: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  mergeSplitFiles(options: { zipFilePath: string; newZipFilePath: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  getAllFileNames(options: { zipFilePath: string; password?: string | undefined; }): Promise<{ names: string[]; }> {
    console.log(options);
    return Promise.resolve({ names: ['extractAll'] });
  }
  isEncrypted(options: { zipFilePath: string; }): Promise<{ isEncrypted: boolean; }> {
    console.log(options);
    return Promise.resolve({ isEncrypted: false });
  }
  isSplitArchive(options: { zipFilePath: string; password?: string | undefined; }): Promise<{ isSplitArchive: boolean; }> {
    console.log(options);
    return Promise.resolve({ isSplitArchive: false });
  }
  setComment(options: { zipFilePath: string; comment: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  getComment(options: { zipFilePath: string; password?: string | undefined; }): Promise<{ comment: string; }> {
    console.log(options);
    return Promise.resolve({ comment: 'extractAll' });
  }
  containsFile(options: { zipFilePath: string; fileName: string; password?: string | undefined; }): Promise<{ contains: boolean; }> {
    console.log(options);
    return Promise.resolve({ contains: false });
  }
  addFiles(options: { zipFilePath: string; files: string[]; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  addFolder(options: { zipFilePath: string; folder: string; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  createSplitZipFile(options: { zipFilePath: string; files: string[]; splitLength: number; password?: string | undefined; }): Promise<{ message: string; }> {
    console.log(options);
    return Promise.resolve({ message: 'not implemented' });
  }
  addListener(
      eventName: 'extractAllProgressEvent' | 'renameFileProgressEvent' | 'removeFilesProgressEvent' | 'renameFileProgressEvent' | 'mergeSplitFilesProgressEvent' | 'setCommentProgressEvent' | 'addFilesProgressEvent' | 'addFolderProgressEvent' | 'createSplitZipFileProgressEvent',
      listenerFunc: (obj: { progress: number }) => void,
    ): Promise<PluginListenerHandle> {
      console.log(eventName);
      console.log(listenerFunc);
      return Promise.resolve(null as any);
    }
}
