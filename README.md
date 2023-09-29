# capacitor-zip4j

capacitor plugin for zip4j (android only)

## Install

```bash
npm install capacitor-zip4j
npx cap sync
```

## API

<docgen-index>

* [`unzip(...)`](#unzip)
* [`addListener('progressEvent', ...)`](#addlistenerprogressevent)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### unzip(...)

```typescript
unzip(options: { source: string; destination: string; password?: string; }) => Promise<{ message: string; }>
```

| Param         | Type                                                                     |
| ------------- | ------------------------------------------------------------------------ |
| **`options`** | <code>{ source: string; destination: string; password?: string; }</code> |

**Returns:** <code>Promise&lt;{ message: string; }&gt;</code>

--------------------


### addListener('progressEvent', ...)

```typescript
addListener(eventName: 'progressEvent', listenerFunc: (obj: { progress: number; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                 |
| ------------------ | ---------------------------------------------------- |
| **`eventName`**    | <code>'progressEvent'</code>                         |
| **`listenerFunc`** | <code>(obj: { progress: number; }) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>
