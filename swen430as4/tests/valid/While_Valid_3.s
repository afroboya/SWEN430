
	.text
wl_sum:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -8(%rbp)
	movq $0, %rax
	movq %rax, -16(%rbp)
	movq -16(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label741
label741:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $0, %rbx
	cmpq %rax, %rbx
	jnz label743
	movq $1, %rax
	jmp label744
label743:
	movq $0, %rax
label744:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $6, %rbx
	cmpq %rax, %rbx
	jnz label745
	movq $1, %rax
	jmp label746
label745:
	movq $0, %rax
label746:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $6, %rbx
	cmpq %rax, %rbx
	jnz label747
	movq $1, %rax
	jmp label748
label747:
	movq $0, %rax
label748:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $1108, %rbx
	cmpq %rax, %rbx
	jnz label749
	movq $1, %rax
	jmp label750
label749:
	movq $0, %rax
label750:
	movq %rax, %rdi
	call assertion
	subq $16, %rsp
	call wl_sum
	addq $16, %rsp
	movq -16(%rsp), %rax
	movq $1108, %rbx
	cmpq %rax, %rbx
	jnz label751
	movq $1, %rax
	jmp label752
label751:
	movq $0, %rax
label752:
	movq %rax, %rdi
	call assertion
label742:
	movq %rbp, %rsp
	popq %rbp
	ret
	.globl main
main:
	pushq %rbp
	call wl_main
	popq %rbp
	movq $0, %rax
	ret

	.data
