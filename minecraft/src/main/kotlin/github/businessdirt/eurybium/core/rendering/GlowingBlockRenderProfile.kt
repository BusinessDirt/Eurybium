package github.businessdirt.eurybium.core.rendering

/** CPU bookkeeping only: GPU execution and the outline post-process are not timed here. */
internal class GlowingBlockRenderProfile {
    var samples = 0L
        private set
    var submittedBlocks = 0
        private set
    var modelParts = 0
        private set
    var preparationTotalNs = 0L
        private set
    var submissionTotalNs = 0L
        private set
    var preparationMaxNs = 0L
        private set
    var submissionMaxNs = 0L
        private set

    fun recordPreparation(elapsedNs: Long, blocks: Int, parts: Int) {
        submittedBlocks = blocks
        modelParts = parts
        preparationTotalNs += elapsedNs
        preparationMaxNs = maxOf(preparationMaxNs, elapsedNs)
    }

    fun recordSubmission(elapsedNs: Long) {
        samples++
        submissionTotalNs += elapsedNs
        submissionMaxNs = maxOf(submissionMaxNs, elapsedNs)
    }

    fun reset() {
        samples = 0
        submittedBlocks = 0
        modelParts = 0
        preparationTotalNs = 0
        submissionTotalNs = 0
        preparationMaxNs = 0
        submissionMaxNs = 0
    }
}
