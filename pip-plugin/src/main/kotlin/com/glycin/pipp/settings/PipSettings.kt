package com.glycin.pipp.settings

import com.intellij.openapi.components.*

@State(name = "PipSettings", storages = [Storage("pipSettings.xml")])
@Service(Service.Level.APP)
class PipSettings: SimplePersistentStateComponent<PipSettings.State>(State()) {
    class State: BaseState() {
        var jsonExportPath by string("C:\\Projects\\pip\\pip-graph\\input.json")
        var memeSaveFolder by string("C:\\Users\\lorda\\Desktop\\Pip\\memes")
    }
}