
	.text
wl_trim:
	pushq %rbp
	movq %rsp, %rbp
	subq $16, %rsp
	movq $0, %rax
	movq %rax, -16(%rbp)
	movq -8(%rbp), %rax
	movq %rax, 16(%rbp)
	jmp label757
label757:
	movq %rbp, %rsp
	popq %rbp
	ret
wl_main:
	pushq %rbp
	movq %rsp, %rbp
	subq $32, %rsp
	movq $2, %rax
	movq %rax, 8(%rsp)
	call wl_trim
	addq $32, %rsp
	movq -32(%rsp), %rax
	jmp label759
	movq $1, %rax
	jmp label760
label759:
	movq $0, %rax
label760:
	movq %rax, %rdi
	call assertion
	subq $32, %rsp
	movq $2, %rax
	movq %rax, 8(%rsp)
	call wl_trim
	addq $32, %rsp
	movq -32(%rsp), %rax
	jmp label761
	movq $1, %rax
	jmp label762
label761:
	movq $0, %rax
label762:
	movq %rax, %rdi
	call assertion
label758:
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
