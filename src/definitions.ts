import type { PluginListenerHandle } from '@capacitor/core';
import type { Plugin } from '@capacitor/core/types/definitions';

export interface Zip4JPlugin extends Plugin {
  unzip(options: { source: string, destination: string, password?: string }): Promise<{ message: string }>;
  addListener(
    eventName: 'progressEvent',
    listenerFunc: (obj: { progress: number}) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
